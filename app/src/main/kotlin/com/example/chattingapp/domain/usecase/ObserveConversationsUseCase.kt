package com.example.chattingapp.domain.usecase

import com.example.chattingapp.data.repository.ConversationRepository

class ObserveConversationsUseCase(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(currentUserId: String) =
        conversationRepository.observeConversations(currentUserId)
}