package com.liegestuetz.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class Challenge(
    val id: String,
    val name: String,
    val description: String?,
    val creatorId: String,
    val startDate: LocalDate,
    val durationDays: Int,
    val startingReps: Int,
    /** Added to the rep goal each day. 0 means a flat challenge (same reps every day). */
    val dailyIncrement: Int,
    /** 6-character uppercase code used to invite friends. */
    val inviteCode: String,
    val participantIds: List<String>,
    val status: ChallengeStatus,
    val createdAt: Instant,
) {
    /**
     * Computes the rep goal for a given day index (0-based, where 0 = startDate).
     *
     * Examples:
     *   startingReps=10, dailyIncrement=5, dayIndex=0 → 10
     *   startingReps=10, dailyIncrement=5, dayIndex=1 → 15
     *   startingReps=10, dailyIncrement=0, dayIndex=7 → 10 (flat)
     */
    fun goalForDayIndex(dayIndex: Int): Int = startingReps + dailyIncrement * dayIndex

    /** True when today is within the challenge's active date range. */
    fun isActiveOn(date: LocalDate): Boolean =
        status == ChallengeStatus.ACTIVE && date >= startDate
}

enum class ChallengeStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED,
}
