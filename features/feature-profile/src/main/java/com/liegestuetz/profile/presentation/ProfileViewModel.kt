package com.liegestuetz.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liegestuetz.domain.model.User
import com.liegestuetz.domain.repository.UserRepository
import com.liegestuetz.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    val uiState = userRepository.getCurrentUser()
        .map { user -> ProfileUiState(user = user, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    private val _signedOut = MutableSharedFlow<Unit>()
    val signedOut = _signedOut.asSharedFlow()

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _signedOut.emit(Unit)
        }
    }
}
