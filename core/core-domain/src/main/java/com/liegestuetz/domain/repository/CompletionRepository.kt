package com.liegestuetz.domain.repository

import com.liegestuetz.common.Result
import com.liegestuetz.domain.model.Completion
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface CompletionRepository {

    /**
     * Writes a completion document to Firestore.
     * The write is idempotent: the document ID is "{userId}_{date}", so calling
     * this twice for the same user+date is a safe no-op (Firestore set with merge).
     */
    suspend fun markDayComplete(completion: Completion): Result<Unit>

    /**
     * Real-time stream of all completions for today across all participants
     * in the given challenge. Used to drive the live leaderboard on ChallengeDetailScreen.
     */
    fun getTodayCompletions(challengeId: String, date: LocalDate): Flow<Result<List<Completion>>>

    /**
     * Real-time stream of a specific user's completion history in a challenge.
     * Used to render the personal heatmap calendar strip.
     */
    fun getCompletionHistory(
        challengeId: String,
        userId: String,
    ): Flow<Result<List<Completion>>>

    /**
     * One-shot check: did this user already complete their goal today?
     * Reads a single Firestore document — O(1), offline-capable via Room cache.
     */
    suspend fun hasCompletedToday(
        challengeId: String,
        userId: String,
        date: LocalDate,
    ): Result<Boolean>
}
