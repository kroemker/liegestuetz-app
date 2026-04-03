package com.liegestuetz.domain.repository

import com.liegestuetz.common.Result
import com.liegestuetz.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Handles Firebase Authentication operations.
 * Implemented in :core:core-data by UserRepositoryImpl alongside UserRepository.
 */
interface AuthRepository {
    /** Emits the current auth user (basic info from Firebase Auth, no Firestore round-trip). Null = signed out. */
    fun currentUser(): Flow<User?>

    suspend fun signIn(email: String, password: String): Result<User>

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
    ): Result<User>

    suspend fun signOut()

    fun isSignedIn(): Boolean
}
