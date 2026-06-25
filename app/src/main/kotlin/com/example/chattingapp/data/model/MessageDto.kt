package com.example.chattingapp.data.model

import com.google.firebase.Timestamp

data class MessageDto(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val type: String = "TEXT",
    val text: String = "",
    val attachments: List<AttachmentDto> = emptyList(),
    val createdAt: Timestamp? = null,
    val editedAt: Timestamp? = null,
    val deletedAt: Timestamp? = null,
    val status: String = "SENT",
    val readBy: Map<String, Timestamp> = emptyMap()
)