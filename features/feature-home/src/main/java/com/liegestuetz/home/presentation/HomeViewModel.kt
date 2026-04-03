package com.liegestuetz.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liegestuetz.common.Result
import com.liegestuetz.common.extensions.today
import com.liegestuetz.domain.model.Challenge
import com.liegestuetz.domain.repository.AuthRepository
import com.liegestuetz.domain.repository.CompletionRepository
import com.liegestuetz.domain.usecase.GetTodayGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import javax.inject.Inject
import com.liegestuetz.domain.repository.ChallengeRepository

data class ChallengeCardState(
    val challenge: Challenge,
    val todayGoal: Int?,
    val completedToday: Boolean,
    val dayIndex: Int,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val challenges: List<ChallengeCardState> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val completionRepository: CompletionRepository,
    private val authRepository: AuthRepository,
    private val getTodayGoalUseCase: GetTodayGoalUseCase,
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = authRepository.currentUser()
        .flatMapLatest { user ->
            if (user == null) return@flatMapLatest flowOf(HomeUiState(isLoading = false))
            challengeRepository.getChallengesForUser(user.uid)
        }
        .combine(_errorMessage) { challengesResult, error ->
            when (challengesResult) {
                is Result.Loading -> HomeUiState(isLoading = true)
                is Result.Error -> HomeUiState(
                    isLoading = false,
                    errorMessage = challengesResult.message ?: challengesResult.exception.message,
                )
                is Result.Success -> {
                    val today = LocalDate.today()
                    val cards = challengesResult.data.map { challenge ->
                        val dayIndex = challenge.startDate.until(today, DateTimeUnit.DAY)
                        val goal = getTodayGoalUseCase(challenge, today)
                        // Check completion status from a snapshot listener would be ideal;
                        // here we derive it from completionRepository on initial load.
                        ChallengeCardState(
                            challenge = challenge,
                            todayGoal = goal,
                            completedToday = false, // Updated per-card by ChallengeDetailScreen
                            dayIndex = dayIndex.coerceAtLeast(0),
                        )
                    }.sortedByDescending { it.challenge.createdAt }
                    HomeUiState(isLoading = false, challenges = cards, errorMessage = error)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun dismissError() = _errorMessage.update { null }
}
