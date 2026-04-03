package com.liegestuetz.domain.model

data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?,
    /** Current FCM registration token. Updated on every app launch. */
    val fcmToken: String?,
)
