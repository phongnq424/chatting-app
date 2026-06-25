package com.example.chattingapp.domain.usecase

import com.example.chattingapp.data.repository.AuthRepository

class UpdateFcmTokenUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(token: String) {
        authRepository.updateFCMToken(token)
    }
}