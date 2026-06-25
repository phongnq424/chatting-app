package com.example.chattingapp.domain.usecase

import com.example.chattingapp.data.repository.ConversationRepository

class MarkAsReadUseCase(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        userId: String
    ) {
        conversationRepository.markAsRead(
            conversationId = conversationId,
            userId = userId
        )
    }
}