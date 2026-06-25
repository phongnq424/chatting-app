package com.example.chattingapp.domain.usecase

import com.example.chattingapp.data.repository.MessageRepository
import com.google.firebase.auth.FirebaseUser

class SendMessageUseCase(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        sender: FirebaseUser,
        text: String
    ) {
        messageRepository.sendTextMessage(
            conversationId = conversationId,
            sender = sender,
            text = text
        )
    }
}