package com.liegestuetz.domain.repository

import com.liegestuetz.common.Result
import com.liegestuetz.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    /** Emits the currently signed-in user, or null when signed out. Hot Flow. */
    fun getCurrentUser(): Flow<User?>

    suspend fun getUserById(userId: String): Result<User>

    /** Creates the Firestore user document on first sign-in. */
    suspend fun createOrUpdateUser(user: User): Result<Unit>

    /** Called on every app launch after FCM token refresh. */
    suspend fun updateFcmToken(userId: String, token: String): Result<Unit>

    suspend fun signOut()
}
