package com.liegestuetz.domain.repository

import com.liegestuetz.common.Result
import com.liegestuetz.domain.model.Challenge
import com.liegestuetz.domain.model.Participant
import kotlinx.coroutines.flow.Flow

interface ChallengeRepository {

    /** Real-time stream of challenges the current user participates in. */
    fun getChallengesForUser(userId: String): Flow<Result<List<Challenge>>>

    /** Real-time stream of a single challenge by ID. */
    fun getChallengeById(challengeId: String): Flow<Result<Challenge>>

    /**
     * Creates a new challenge in Firestore and adds the creator as the first participant.
     * Returns the created challenge (with its Firestore-assigned ID and generated invite code).
     */
    suspend fun createChallenge(challenge: Challenge): Result<Challenge>

    /**
     * Looks up a challenge by its [inviteCode] and adds [userId] as a participant.
     * Returns the challenge the user just joined.
     */
    suspend fun joinChallenge(inviteCode: String, userId: String): Result<Challenge>

    suspend fun updateChallenge(challenge: Challenge): Result<Unit>

    suspend fun cancelChallenge(challengeId: String): Result<Unit>

    /** Real-time stream of participants in a challenge, ordered by totalCompletions desc. */
    fun getParticipants(challengeId: String): Flow<Result<List<Participant>>>
}
