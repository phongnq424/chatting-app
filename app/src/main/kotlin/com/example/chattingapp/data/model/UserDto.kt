package com.example.chattingapp.data.model

import com.google.firebase.Timestamp

data class UserDto(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val fcmToken: String = "",
    val isOnline: Boolean = false,
    val lastSeenAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)