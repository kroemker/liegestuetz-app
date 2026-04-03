package com.liegestuetz.domain.usecase

import com.liegestuetz.common.Result
import com.liegestuetz.domain.model.User
import com.liegestuetz.domain.repository.AuthRepository
import com.liegestuetz.domain.repository.UserRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank()) return Result.Error(IllegalArgumentException("Email is required"))
        if (password.isBlank()) return Result.Error(IllegalArgumentException("Password is required"))
        val result = authRepository.signIn(email.trim(), password)
        // Ensure Firestore user document exists after sign-in
        if (result is Result.Success) {
            userRepository.createOrUpdateUser(result.data)
        }
        return result
    }
}
