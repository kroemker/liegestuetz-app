package com.liegestuetz.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.liegestuetz.common.Result
import com.liegestuetz.common.runCatchingResult
import com.liegestuetz.data.remote.dto.ChallengeDto
import com.liegestuetz.data.remote.dto.ParticipantDto
import com.liegestuetz.data.remote.dto.toDto
import com.liegestuetz.domain.model.Challenge
import com.liegestuetz.domain.model.Participant
import com.liegestuetz.domain.repository.ChallengeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHALLENGES = "challenges"
private const val PARTICIPANTS = "participants"

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ChallengeRepository {

    override fun getChallengesForUser(userId: String): Flow<Result<List<Challenge>>> =
        callbackFlow {
            trySend(Result.Loading)
            val registration = firestore.collection(CHALLENGES)
                .whereArrayContains("participantIds", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(Result.Error(error)); return@addSnapshotListener }
                    val list = snapshot?.documents?.mapNotNull {
                        it.toObject(ChallengeDto::class.java)?.toDomain()
                    } ?: emptyList()
                    trySend(Result.Success(list))
                }
            awaitClose { registration.remove() }
        }.flowOn(Dispatchers.IO)

    override fun getChallengeById(challengeId: String): Flow<Result<Challenge>> =
        callbackFlow {
            trySend(Result.Loading)
            val registration = firestore.collection(CHALLENGES).document(challengeId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(Result.Error(error)); return@addSnapshotListener }
                    val challenge = snapshot?.toObject(ChallengeDto::class.java)?.toDomain()
                    if (challenge != null) trySend(Result.Success(challenge))
                    else trySend(Result.Error(NoSuchElementException("Challenge not found")))
                }
            awaitClose { registration.remove() }
        }.flowOn(Dispatchers.IO)

    override suspend fun createChallenge(challenge: Challenge): Result<Challenge> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                val colRef = firestore.collection(CHALLENGES)
                val docRef = colRef.document()
                val challengeWithId = challenge.copy(id = docRef.id)

                firestore.batch().apply {
                    set(docRef, challengeWithId.toDto())
                    set(
                        docRef.collection(PARTICIPANTS).document(challenge.creatorId),
                        ParticipantDto(
                            userId = challenge.creatorId,
                            displayName = auth.currentUser?.displayName ?: "",
                            photoUrl = auth.currentUser?.photoUrl?.toString(),
                            joinedAt = Timestamp.now(),
                        )
                    )
                }.await()

                challengeWithId
            }
        }

    override suspend fun joinChallenge(inviteCode: String, userId: String): Result<Challenge> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                val querySnap = firestore.collection(CHALLENGES)
                    .whereEqualTo("inviteCode", inviteCode)
                    .whereEqualTo("status", "active")
                    .limit(1)
                    .get().await()

                if (querySnap.isEmpty) throw IllegalArgumentException("Invalid or expired invite code")

                val challengeDoc = querySnap.documents.first()
                val challenge = challengeDoc.toObject(ChallengeDto::class.java)!!.toDomain()

                if (userId in challenge.participantIds) return@runCatchingResult challenge

                firestore.batch().apply {
                    update(challengeDoc.reference, "participantIds", FieldValue.arrayUnion(userId))
                    set(
                        challengeDoc.reference.collection(PARTICIPANTS).document(userId),
                        ParticipantDto(
                            userId = userId,
                            displayName = auth.currentUser?.displayName ?: "",
                            photoUrl = auth.currentUser?.photoUrl?.toString(),
                            joinedAt = Timestamp.now(),
                        )
                    )
                }.await()

                challenge.copy(participantIds = challenge.participantIds + userId)
            }
        }

    override suspend fun updateChallenge(challenge: Challenge): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                firestore.collection(CHALLENGES).document(challenge.id)
                    .set(challenge.toDto()).await()
            }
        }

    override suspend fun cancelChallenge(challengeId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                firestore.collection(CHALLENGES).document(challengeId)
                    .update("status", "cancelled").await()
            }
        }

    override fun getParticipants(challengeId: String): Flow<Result<List<Participant>>> =
        callbackFlow {
            trySend(Result.Loading)
            val registration = firestore.collection(CHALLENGES)
                .document(challengeId)
                .collection(PARTICIPANTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(Result.Error(error)); return@addSnapshotListener }
                    val list = snapshot?.toObjects(ParticipantDto::class.java)
                        ?.map { it.toDomain() }
                        ?.sortedByDescending { it.totalCompletions }
                        ?: emptyList()
                    trySend(Result.Success(list))
                }
            awaitClose { registration.remove() }
        }.flowOn(Dispatchers.IO)
}
