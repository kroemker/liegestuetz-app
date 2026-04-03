package com.liegestuetz.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.liegestuetz.common.Result
import com.liegestuetz.common.runCatchingResult
import com.liegestuetz.data.remote.dto.UserDto
import com.liegestuetz.data.remote.dto.toDto
import com.liegestuetz.domain.model.User
import com.liegestuetz.domain.repository.AuthRepository
import com.liegestuetz.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val USERS_COLLECTION = "users"

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository, UserRepository {

    // ── AuthRepository ───────────────────────────────────────────────────────

    override fun currentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val fbUser = firebaseAuth.currentUser
            trySend(
                fbUser?.let {
                    User(
                        uid = it.uid,
                        displayName = it.displayName ?: "",
                        email = it.email ?: "",
                        photoUrl = it.photoUrl?.toString(),
                        fcmToken = null,
                    )
                }
            )
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun isSignedIn(): Boolean = auth.currentUser != null

    override suspend fun signIn(email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                val credential = auth.signInWithEmailAndPassword(email, password).await()
                val fbUser = credential.user ?: throw FirebaseAuthException("", "Sign-in returned no user")
                User(
                    uid = fbUser.uid,
                    displayName = fbUser.displayName ?: "",
                    email = fbUser.email ?: "",
                    photoUrl = fbUser.photoUrl?.toString(),
                    fcmToken = null,
                )
            }
        }

    override suspend fun register(email: String, password: String, displayName: String): Result<User> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                val credential = auth.createUserWithEmailAndPassword(email, password).await()
                val fbUser = credential.user ?: throw FirebaseAuthException("", "Registration returned no user")
                // Set display name on the Firebase Auth profile
                fbUser.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
                ).await()
                User(
                    uid = fbUser.uid,
                    displayName = displayName,
                    email = fbUser.email ?: email,
                    photoUrl = null,
                    fcmToken = null,
                )
            }
        }

    override suspend fun signOut() {
        auth.signOut()
    }

    // ── UserRepository ───────────────────────────────────────────────────────

    override fun getCurrentUser(): Flow<User?> = callbackFlow<String?> {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.flatMapLatest { uid ->
        if (uid == null) {
            flowOf(null)
        } else {
            callbackFlow {
                val registration = firestore.collection(USERS_COLLECTION).document(uid)
                    .addSnapshotListener { snap, _ ->
                        trySend(snap?.toObject(UserDto::class.java)?.toDomain())
                    }
                awaitClose { registration.remove() }
            }
        }
    }

    override suspend fun getUserById(userId: String): Result<User> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                val snap = firestore.collection(USERS_COLLECTION).document(userId).get().await()
                snap.toObject(UserDto::class.java)?.toDomain()
                    ?: throw NoSuchElementException("User $userId not found")
            }
        }

    override suspend fun createOrUpdateUser(user: User): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                firestore.collection(USERS_COLLECTION)
                    .document(user.uid)
                    .set(user.toDto())
                    .await()
            }
        }

    override suspend fun updateFcmToken(userId: String, token: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .update("fcmToken", token)
                    .await()
            }
        }
}
