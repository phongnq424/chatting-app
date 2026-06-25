package com.example.chattingapp.domain.model

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderPhotoUrl: String = "",
    val type: MessageType = MessageType.TEXT,
    val text: String = "",
    val attachments: List<Attachment> = emptyList(),
    val createdAt: Long = 0L,
    val editedAt: Long? = null,
    val deletedAt: Long? = null,
    val status: MessageStatus = MessageStatus.SENT,
    val readBy: Map<String, Long> = emptyMap()
)

enum class MessageType {
    TEXT,
    IMAGE,
    FILE,
    SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    FAILED,
    READ
}