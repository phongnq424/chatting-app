package com.example.chattingapp.data.mapper

import com.example.chattingapp.data.model.AttachmentDto
import com.example.chattingapp.data.model.ConversationDto
import com.example.chattingapp.data.model.MessageDto
import com.example.chattingapp.data.model.UserDto
import com.example.chattingapp.domain.model.Attachment
import com.example.chattingapp.domain.model.AttachmentType
import com.example.chattingapp.domain.model.Conversation
import com.example.chattingapp.domain.model.ConversationType
import com.example.chattingapp.domain.model.Message
import com.example.chattingapp.domain.model.MessageStatus
import com.example.chattingapp.domain.model.MessageType
import com.example.chattingapp.domain.model.User
import com.google.firebase.Timestamp

fun Timestamp?.toMillis(): Long {
    return this?.toDate()?.time ?: 0L
}

fun UserDto.toDomain(): User {
    return User(
        id = uid,
        email = email,
        displayName = displayName.ifBlank { "User ${uid.take(4)}" },
        photoUrl = photoUrl,
        isOnline = isOnline,
        lastSeenAt = lastSeenAt.toMillis()
    )
}

fun ConversationDto.toDomain(fallbackId: String = ""): Conversation {
    return Conversation(
        id = id.ifBlank { fallbackId },
        type = runCatching { ConversationType.valueOf(type) }.getOrDefault(ConversationType.DIRECT),
        title = title,
        photoUrl = photoUrl,
        memberIds = memberIds,
        lastMessageText = lastMessageText,
        lastMessageAt = lastMessageAt.toMillis(),
        lastSenderId = lastSenderId,
        createdAt = createdAt.toMillis(),
        updatedAt = updatedAt.toMillis()
    )
}

fun AttachmentDto.toDomain(): Attachment {
    return Attachment(
        url = url,
        type = runCatching { AttachmentType.valueOf(type) }.getOrDefault(AttachmentType.FILE),
        fileName = fileName,
        sizeBytes = sizeBytes
    )
}

fun MessageDto.toDomain(fallbackId: String = ""): Message {
    return Message(
        id = id.ifBlank { fallbackId },
        conversationId = conversationId,
        senderId = senderId,
        senderName = senderName,
        senderPhotoUrl = senderPhotoUrl,
        type = runCatching { MessageType.valueOf(type) }.getOrDefault(MessageType.TEXT),
        text = text,
        attachments = attachments.map { it.toDomain() },
        createdAt = createdAt.toMillis(),
        editedAt = editedAt?.toDate()?.time,
        deletedAt = deletedAt?.toDate()?.time,
        status = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT),
        readBy = readBy.mapValues { it.value.toMillis() }
    )
}