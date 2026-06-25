package com.example.chattingapp.data.model

import com.google.firebase.Timestamp

data class ConversationDto(
    val id: String = "",
    val type: String = "DIRECT",
    val title: String = "",
    val photoUrl: String = "",
    val memberIds: List<String> = emptyList(),
    val lastMessageText: String = "",
    val lastMessageAt: Timestamp? = null,
    val lastSenderId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)