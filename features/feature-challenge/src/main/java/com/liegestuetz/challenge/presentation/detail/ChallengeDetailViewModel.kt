package com.liegestuetz.challenge.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liegestuetz.common.Result
import com.liegestuetz.common.extensions.today
import com.liegestuetz.domain.model.Challenge
import com.liegestuetz.domain.model.Completion
import com.liegestuetz.domain.model.Participant
import com.liegestuetz.domain.repository.AuthRepository
import com.liegestuetz.domain.repository.ChallengeRepository
import com.liegestuetz.domain.repository.CompletionRepository
import com.liegestuetz.domain.usecase.GetTodayGoalUseCase
import com.liegestuetz.domain.usecase.MarkDayCompleteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import javax.inject.Inject

data class ParticipantRow(
    val participant: Participant,
    val completedToday: Boolean,
)

data class ChallengeDetailUiState(
    val isLoading: Boolean = true,
    val challenge: Challenge? = null,
    val todayGoal: Int? = null,
    val dayIndex: Int = 0,
    val currentUserId: String? = null,
    val completedToday: Boolean = false,
    val isMarkingComplete: Boolean = false,
    val participants: List<ParticipantRow> = emptyList(),
    val completionHistory: List<Completion> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class ChallengeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val challengeRepository: ChallengeRepository,
    private val completionRepository: CompletionRepository,
    private val authRepository: AuthRepository,
    private val getTodayGoalUseCase: GetTodayGoalUseCase,
    private val markDayCompleteUseCase: MarkDayCompleteUseCase,
) : ViewModel() {

    private val challengeId: String = checkNotNull(savedStateHandle["challengeId"])
    private val today: LocalDate = LocalDate.today()

    private val _markingComplete = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChallengeDetailUiState> = combine(
        challengeRepository.getChallengeById(challengeId),
        challengeRepository.getParticipants(challengeId),
        completionRepository.getTodayCompletions(challengeId, today),
        completionRepository.getCompletionHistory(challengeId, authRepository.currentUser().first()?.uid ?: ""),
        authRepository.currentUser(),
        _markingComplete,
        _errorMessage,
    ) { results ->
        @Suppress("UNCHECKED_CAST")
        val challengeResult = results[0] as Result<Challenge>
        @Suppress("UNCHECKED_CAST")
        val participantsResult = results[1] as Result<List<Participant>>
        @Suppress("UNCHECKED_CAST")
        val todayCompletionsResult = results[2] as Result<List<Completion>>
        @Suppress("UNCHECKED_CAST")
        val historyResult = results[3] as Result<List<Completion>>
        val currentUser = results[4] as com.liegestuetz.domain.model.User?
        val isMarking = results[5] as Boolean
        val error = results[6] as String?

        if (challengeResult is Result.Error) {
            return@combine ChallengeDetailUiState(
                isLoading = false,
                errorMessage = challengeResult.exception.message,
            )
        }
        if (challengeResult !is Result.Success) return@combine ChallengeDetailUiState(isLoading = true)

        val challenge = challengeResult.data
        val participants = (participantsResult as? Result.Success)?.data ?: emptyList()
        val todayCompletions = (todayCompletionsResult as? Result.Success)?.data ?: emptyList()
        val history = (historyResult as? Result.Success)?.data ?: emptyList()
        val uid = currentUser?.uid ?: ""

        val completedUserIds = todayCompletions.map { it.userId }.toSet()
        val dayIndex = challenge.startDate.until(today, DateTimeUnit.DAY).coerceAtLeast(0)

        ChallengeDetailUiState(
            isLoading = false,
            challenge = challenge,
            todayGoal = getTodayGoalUseCase(challenge, today),
            dayIndex = dayIndex,
            currentUserId = uid,
            completedToday = uid in completedUserIds,
            isMarkingComplete = isMarking,
            participants = participants.map { p ->
                ParticipantRow(p, completedToday = p.userId in completedUserIds)
            },
            completionHistory = history,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChallengeDetailUiState())

    fun markComplete() {
        viewModelScope.launch {
            val s = uiState.value
            val goal = s.todayGoal ?: return@launch
            val uid = s.currentUserId ?: return@launch
            _markingComplete.update { true }
            val result = markDayCompleteUseCase(challengeId, uid, goal)
            _markingComplete.update { false }
            if (result is Result.Error) {
                _errorMessage.update { result.message ?: result.exception.message }
            }
        }
    }

    fun dismissError() = _errorMessage.update { null }
}
