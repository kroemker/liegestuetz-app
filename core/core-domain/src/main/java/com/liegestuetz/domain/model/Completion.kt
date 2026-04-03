package com.liegestuetz.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Records that a user completed their pushup goal for a specific date in a challenge.
 *
 * Firestore document ID convention: "{userId}_{YYYY-MM-DD}"
 * This makes existence checks O(1) and writes naturally idempotent.
 */
data class Completion(
    val userId: String,
    val challengeId: String,
    /** The local calendar date the pushups were performed. */
    val date: LocalDate,
    /** Snapshot of the rep goal at the time of completion (in case the challenge is later modified). */
    val goalReps: Int,
    val completedAt: Instant,
    val note: String?,
) {
    /** Firestore document ID for this completion. */
    val documentId: String get() = "${userId}_${date}"
}
