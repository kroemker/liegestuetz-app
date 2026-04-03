package com.liegestuetz.domain.usecase

import com.liegestuetz.common.Result
import com.liegestuetz.domain.model.Challenge
import com.liegestuetz.domain.repository.ChallengeRepository
import javax.inject.Inject

class JoinChallengeUseCase @Inject constructor(
    private val challengeRepository: ChallengeRepository,
) {
    suspend operator fun invoke(inviteCode: String, userId: String): Result<Challenge> {
        val code = inviteCode.trim().uppercase()
        if (code.length != 6) return Result.Error(IllegalArgumentException("Invite code must be 6 characters"))
        return challengeRepository.joinChallenge(code, userId)
    }
}
