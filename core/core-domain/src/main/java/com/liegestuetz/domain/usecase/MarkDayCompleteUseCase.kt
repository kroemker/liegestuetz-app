package com.liegestuetz.domain.usecase

import com.liegestuetz.common.Result
import com.liegestuetz.common.extensions.today
import com.liegestuetz.domain.model.Completion
import com.liegestuetz.domain.repository.CompletionRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class MarkDayCompleteUseCase @Inject constructor(
    private val completionRepository: CompletionRepository,
) {
    suspend operator fun invoke(
        challengeId: String,
        userId: String,
        goalReps: Int,
        note: String? = null,
    ): Result<Unit> {
        val today = LocalDate.today()

        // Idempotency: if already completed today, treat as success
        val alreadyDone = completionRepository.hasCompletedToday(challengeId, userId, today)
        if (alreadyDone is Result.Success && alreadyDone.data) {
            return Result.Success(Unit)
        }

        val completion = Completion(
            userId = userId,
            challengeId = challengeId,
            date = today,
            goalReps = goalReps,
            completedAt = Clock.System.now(),
            note = note,
        )
        return completionRepository.markDayComplete(completion)
    }
}
