package com.liegestuetz.challenge.presentation.join

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liegestuetz.common.Result
import com.liegestuetz.domain.repository.AuthRepository
import com.liegestuetz.domain.usecase.JoinChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JoinChallengeUiState(
    val inviteCode: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class JoinChallengeViewModel @Inject constructor(
    private val joinChallengeUseCase: JoinChallengeUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinChallengeUiState())
    val uiState: StateFlow<JoinChallengeUiState> = _uiState.asStateFlow()

    private val _navigateToChallengeId = MutableSharedFlow<String>()
    val navigateToChallengeId = _navigateToChallengeId.asSharedFlow()

    fun onCodeChanged(value: String) {
        _uiState.update { it.copy(inviteCode = value.uppercase().take(6)) }
    }

    fun onJoin() {
        viewModelScope.launch {
            val user = authRepository.currentUser().first() ?: return@launch
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = joinChallengeUseCase(_uiState.value.inviteCode, user.uid)) {
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

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }
}
