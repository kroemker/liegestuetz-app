package com.liegestuetz.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.liegestuetz.domain.model.Challenge
import com.liegestuetz.domain.model.ChallengeStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class ChallengeDto(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String? = null,
    val creatorId: String = "",
    /** ISO-8601 date string "YYYY-MM-DD" */
    val startDate: String = "",
    val durationDays: Int = 30,
    val startingReps: Int = 10,
    val dailyIncrement: Int = 0,
    val inviteCode: String = "",
    val participantIds: List<String> = emptyList(),
    val status: String = "active",
    val createdAt: Timestamp = Timestamp.now(),
) {
    fun toDomain(): Challenge = Challenge(
        id = id,
        name = name,
        description = description,
        creatorId = creatorId,
        startDate = LocalDate.parse(startDate),
        durationDays = durationDays,
        startingReps = startingReps,
        dailyIncrement = dailyIncrement,
        inviteCode = inviteCode,
        participantIds = participantIds,
        status = when (status) {
            "completed" -> ChallengeStatus.COMPLETED
            "cancelled" -> ChallengeStatus.CANCELLED
            else -> ChallengeStatus.ACTIVE
        },
        createdAt = Instant.fromEpochSeconds(createdAt.seconds, createdAt.nanoseconds.toLong()),
    )
}

fun Challenge.toDto(): ChallengeDto = ChallengeDto(
    id = id,
    name = name,
    description = description,
    creatorId = creatorId,
    startDate = startDate.toString(),
    durationDays = durationDays,
    startingReps = startingReps,
    dailyIncrement = dailyIncrement,
    inviteCode = inviteCode,
    participantIds = participantIds,
    status = status.name.lowercase(),
    createdAt = Timestamp(createdAt.epochSeconds, createdAt.nanosecondsOfSecond),
)
