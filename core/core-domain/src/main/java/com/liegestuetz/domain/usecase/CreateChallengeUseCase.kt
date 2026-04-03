package com.liegestuetz.domain.usecase

import com.liegestuetz.common.Result
import com.liegestuetz.domain.model.Challenge
import com.liegestuetz.domain.model.ChallengeStatus
import com.liegestuetz.domain.repository.ChallengeRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class CreateChallengeParams(
    val name: String,
    val description: String?,
    val creatorId: String,
    val creatorDisplayName: String,
    val creatorPhotoUrl: String?,
    val startDate: LocalDate,
    val durationDays: Int,
    val startingReps: Int,
    val dailyIncrement: Int,
)

class CreateChallengeUseCase @Inject constructor(
    private val challengeRepository: ChallengeRepository,
) {
    suspend operator fun invoke(params: CreateChallengeParams): Result<Challenge> {
        if (params.name.isBlank()) return Result.Error(IllegalArgumentException("Challenge name is required"))
        if (params.startingReps <= 0) return Result.Error(IllegalArgumentException("Starting reps must be at least 1"))
        if (params.dailyIncrement < 0) return Result.Error(IllegalArgumentException("Daily increment can't be negative"))
        if (params.durationDays <= 0) return Result.Error(IllegalArgumentException("Duration must be at least 1 day"))

        val challenge = Challenge(
            id = "",
            name = params.name.trim(),
            description = params.description?.trim()?.ifBlank { null },
            creatorId = params.creatorId,
            startDate = params.startDate,
            durationDays = params.durationDays,
            startingReps = params.startingReps,
            dailyIncrement = params.dailyIncrement,
            inviteCode = generateInviteCode(),
            participantIds = listOf(params.creatorId),
            status = ChallengeStatus.ACTIVE,
            createdAt = Clock.System.now(),
        )
        return challengeRepository.createChallenge(challenge)
    }

    private fun generateInviteCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..6).map { chars.random() }.joinToString("")
    }
}
