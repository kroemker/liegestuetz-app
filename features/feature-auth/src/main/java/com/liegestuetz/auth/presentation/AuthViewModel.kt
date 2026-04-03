package com.liegestuetz.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liegestuetz.common.Result
import com.liegestuetz.domain.repository.AuthRepository
import com.liegestuetz.domain.usecase.RegisterUseCase
import com.liegestuetz.domain.usecase.SignInUseCase
import com.liegestuetz.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface AuthUiEvent {
    data class SignIn(val email: String, val password: String) : AuthUiEvent
    data class Register(val email: String, val password: String, val displayName: String) : AuthUiEvent
    data object SignOut : AuthUiEvent
    data object ErrorDismissed : AuthUiEvent
}

sealed interface AuthNavEvent {
    data object NavigateToHome : AuthNavEvent
    data object NavigateToLogin : AuthNavEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val registerUseCase: RegisterUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(isSignedIn = authRepository.isSignedIn()))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _navEvents = MutableSharedFlow<AuthNavEvent>()
    val navEvents = _navEvents.asSharedFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.SignIn -> signIn(event.email, event.password)
            is AuthUiEvent.Register -> register(event.email, event.password, event.displayName)
            is AuthUiEvent.SignOut -> signOut()
            is AuthUiEvent.ErrorDismissed -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = signInUseCase(email, password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSignedIn = true) }
                    _navEvents.emit(AuthNavEvent.NavigateToHome)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: result.exception.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = registerUseCase(email, password, displayName)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSignedIn = true) }
                    _navEvents.emit(AuthNavEvent.NavigateToHome)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: result.exception.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _uiState.update { it.copy(isSignedIn = false) }
            _navEvents.emit(AuthNavEvent.NavigateToLogin)
        }
    }
}
