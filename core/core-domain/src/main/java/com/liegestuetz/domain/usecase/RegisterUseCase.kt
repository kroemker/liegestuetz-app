package com.liegestuetz.domain.usecase

import com.liegestuetz.common.Result
import com.liegestuetz.domain.model.User
import com.liegestuetz.domain.repository.AuthRepository
import com.liegestuetz.domain.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String,
    ): Result<User> {
        if (displayName.isBlank()) return Result.Error(IllegalArgumentException("Display name is required"))
        if (email.isBlank()) return Result.Error(IllegalArgumentException("Email is required"))
        if (password.length < 6) return Result.Error(IllegalArgumentException("Password must be at least 6 characters"))

        val result = authRepository.register(email.trim(), password, displayName.trim())
        if (result is Result.Success) {
            userRepository.createOrUpdateUser(result.data)
        }
        return result
    }
}
