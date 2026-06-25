package com.example.chattingapp.domain.model

data class Conversation(
    val id: String,
    val type: ConversationType,
    val title: String,
    val photoUrl: String = "",
    val memberIds: List<String> = emptyList(),
    val lastMessageText: String = "",
    val lastMessageAt: Long = 0L,
    val lastSenderId: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

enum class ConversationType {
    DIRECT,
    GROUP
}