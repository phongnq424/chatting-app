package com.example.chattingapp.domain.usecase

import com.example.chattingapp.data.repository.MessageRepository

class ObserveMessagesUseCase(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(conversationId: String) =
        messageRepository.observeMessages(conversationId)
}