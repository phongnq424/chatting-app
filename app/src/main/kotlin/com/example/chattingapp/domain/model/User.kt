package com.example.chattingapp.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeenAt: Long = 0L
)