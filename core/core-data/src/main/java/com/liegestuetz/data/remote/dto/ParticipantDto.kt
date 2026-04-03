package com.liegestuetz.data.remote.dto

import com.google.firebase.Timestamp
import com.liegestuetz.domain.model.Participant
import kotlinx.datetime.Instant

data class ParticipantDto(
    val userId: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val joinedAt: Timestamp = Timestamp.now(),
    val totalCompletions: Int = 0,
    val currentStreak: Int = 0,
) {
    fun toDomain(): Participant = Participant(
        userId = userId,
        displayName = displayName,
        photoUrl = photoUrl,
        joinedAt = Instant.fromEpochSeconds(joinedAt.seconds, joinedAt.nanoseconds.toLong()),
        totalCompletions = totalCompletions,
        currentStreak = currentStreak,
    )
}
