package com.liegestuetz.domain.model

import kotlinx.datetime.Instant

data class Participant(
    val userId: String,
    val displayName: String,
    val photoUrl: String?,
    val joinedAt: Instant,
    /** Cached counter — updated server-side by Cloud Function on each completion. */
    val totalCompletions: Int,
    /** Current consecutive-day streak — updated server-side by Cloud Function. */
    val currentStreak: Int,
)
