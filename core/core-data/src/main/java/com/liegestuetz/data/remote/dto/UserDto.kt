package com.liegestuetz.data.remote.dto

import com.google.firebase.firestore.DocumentId
import com.liegestuetz.domain.model.User

data class UserDto(
    @DocumentId val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val fcmToken: String? = null,
) {
    fun toDomain(): User = User(
        uid = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl,
        fcmToken = fcmToken,
    )
}

fun User.toDto(): UserDto = UserDto(
    uid = uid,
    displayName = displayName,
    email = email,
    photoUrl = photoUrl,
    fcmToken = fcmToken,
)
