package com.liegestuetz.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.liegestuetz.common.Result
import com.liegestuetz.common.runCatchingResult
import com.liegestuetz.data.remote.dto.CompletionDto
import com.liegestuetz.data.remote.dto.toDto
import com.liegestuetz.domain.model.Completion
import com.liegestuetz.domain.repository.CompletionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val CHALLENGES = "challenges"
private const val COMPLETIONS = "completions"

@Singleton
class CompletionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : CompletionRepository {

    override suspend fun markDayComplete(completion: Completion): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                firestore.collection(CHALLENGES)
                    .document(completion.challengeId)
                    .collection(COMPLETIONS)
                    .document(completion.documentId)
                    .set(completion.toDto())
                    .await()
            }
        }

    override fun getTodayCompletions(
        challengeId: String,
        date: LocalDate,
    ): Flow<Result<List<Completion>>> = callbackFlow {
        trySend(Result.Loading)
        val dateStr = date.toString()
        val registration = firestore.collection(CHALLENGES)
            .document(challengeId)
            .collection(COMPLETIONS)
            .whereEqualTo("date", dateStr)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(Result.Error(error)); return@addSnapshotListener }
                val list = snapshot?.toObjects(CompletionDto::class.java)
                    ?.map { it.toDomain() } ?: emptyList()
                trySend(Result.Success(list))
            }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    override fun getCompletionHistory(
        challengeId: String,
        userId: String,
    ): Flow<Result<List<Completion>>> = callbackFlow {
        trySend(Result.Loading)
        val registration = firestore.collection(CHALLENGES)
            .document(challengeId)
            .collection(COMPLETIONS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(Result.Error(error)); return@addSnapshotListener }
                val list = snapshot?.toObjects(CompletionDto::class.java)
                    ?.map { it.toDomain() }
                    ?.sortedBy { it.date }
                    ?: emptyList()
                trySend(Result.Success(list))
            }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun hasCompletedToday(
        challengeId: String,
        userId: String,
        date: LocalDate,
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatchingResult {
            val docId = "${userId}_${date}"
            val snap = firestore.collection(CHALLENGES)
                .document(challengeId)
                .collection(COMPLETIONS)
                .document(docId)
                .get().await()
            snap.exists()
        }
    }
}
