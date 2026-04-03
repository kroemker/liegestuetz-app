package com.liegestuetz.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.liegestuetz.domain.model.Completion
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class CompletionDto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val challengeId: String = "",
    /** ISO-8601 "YYYY-MM-DD" */
    val date: String = "",
    val goalReps: Int = 0,
    val completedAt: Timestamp = Timestamp.now(),
    val note: String? = null,
) {
    fun toDomain(): Completion = Completion(
        userId = userId,
        challengeId = challengeId,
        date = LocalDate.parse(date),
        goalReps = goalReps,
        completedAt = Instant.fromEpochSeconds(completedAt.seconds, completedAt.nanoseconds.toLong()),
        note = note,
    )
}

fun Completion.toDto(): CompletionDto = CompletionDto(
    id = documentId,
    userId = userId,
    challengeId = challengeId,
    date = date.toString(),
    goalReps = goalReps,
    completedAt = Timestamp(completedAt.epochSeconds, completedAt.nanosecondsOfSecond),
    note = note,
)
