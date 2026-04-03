package com.liegestuetz.domain.usecase

import com.liegestuetz.common.Result
import com.liegestuetz.domain.repository.UserRepository
import javax.inject.Inject

class UpdateFcmTokenUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(userId: String, token: String): Result<Unit> =
        userRepository.updateFcmToken(userId, token)
}
