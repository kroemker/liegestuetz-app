package com.liegestuetz.challenge.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liegestuetz.common.Result
import com.liegestuetz.common.extensions.today
import com.liegestuetz.domain.repository.AuthRepository
import com.liegestuetz.domain.usecase.CreateChallengeParams
import com.liegestuetz.domain.usecase.CreateChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class CreateChallengeUiState(
    val name: String = "",
    val description: String = "",
    val startingReps: String = "10",
    val dailyIncrement: String = "5",
    val durationDays: String = "30",
    val startDate: LocalDate = LocalDate.today(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val previewDay1: Int get() = (startingReps.toIntOrNull() ?: 0)
    val previewDay7: Int get() = (startingReps.toIntOrNull() ?: 0) + (dailyIncrement.toIntOrNull() ?: 0) * 6
    val previewDay14: Int get() = (startingReps.toIntOrNull() ?: 0) + (dailyIncrement.toIntOrNull() ?: 0) * 13
}

sealed interface CreateChallengeEvent {
    data class NameChanged(val value: String) : CreateChallengeEvent
    data class DescriptionChanged(val value: String) : CreateChallengeEvent
    data class StartingRepsChanged(val value: String) : CreateChallengeEvent
    data class DailyIncrementChanged(val value: String) : CreateChallengeEvent
    data class DurationDaysChanged(val value: String) : CreateChallengeEvent
    data class StartDateChanged(val value: LocalDate) : CreateChallengeEvent
    data object Submit : CreateChallengeEvent
    data object ErrorDismissed : CreateChallengeEvent
}

@HiltViewModel
class CreateChallengeViewModel @Inject constructor(
    private val createChallengeUseCase: CreateChallengeUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateChallengeUiState())
    val uiState: StateFlow<CreateChallengeUiState> = _uiState.asStateFlow()

    private val _navigateToChallengeId = MutableSharedFlow<String>()
    val navigateToChallengeId = _navigateToChallengeId.asSharedFlow()

    fun onEvent(event: CreateChallengeEvent) {
        when (event) {
            is CreateChallengeEvent.NameChanged -> _uiState.update { it.copy(name = event.value) }
            is CreateChallengeEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value) }
            is CreateChallengeEvent.StartingRepsChanged -> _uiState.update { it.copy(startingReps = event.value.filter(Char::isDigit)) }
            is CreateChallengeEvent.DailyIncrementChanged -> _uiState.update { it.copy(dailyIncrement = event.value.filter(Char::isDigit)) }
            is CreateChallengeEvent.DurationDaysChanged -> _uiState.update { it.copy(durationDays = event.value.filter(Char::isDigit)) }
            is CreateChallengeEvent.StartDateChanged -> _uiState.update { it.copy(startDate = event.value) }
            is CreateChallengeEvent.Submit -> submit()
            is CreateChallengeEvent.ErrorDismissed -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun submit() {
        viewModelScope.launch {
            val user = authRepository.currentUser().first() ?: return@launch
            val s = _uiState.value
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = createChallengeUseCase(
                CreateChallengeParams(
                    name = s.name,
                    description = s.description.ifBlank { null },
                    creatorId = user.uid,
                    creatorDisplayName = user.displayName,
                    creatorPhotoUrl = user.photoUrl,
                    startDate = s.startDate,
                    durationDays = s.durationDays.toIntOrNull() ?: 30,
                    startingReps = s.startingReps.toIntOrNull() ?: 10,
                    dailyIncrement = s.dailyIncrement.toIntOrNull() ?: 0,
                )
            )

            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigateToChallengeId.emit(result.data.id)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: result.exception.message)
                }
                is Result.Loading -> Unit
            }
        }
    }
}
