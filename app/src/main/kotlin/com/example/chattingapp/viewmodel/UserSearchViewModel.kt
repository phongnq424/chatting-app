package com.example.chattingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattingapp.data.repository.AuthRepository
import com.example.chattingapp.data.repository.ConversationRepository
import com.example.chattingapp.data.repository.UserRepository
import com.example.chattingapp.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null,
    val createdConversationId: String? = null
)

class UserSearchViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserSearchUiState())
    val uiState: StateFlow<UserSearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(
            query = value,
            errorMessage = null
        )
    }

    fun searchUsers() {
        val email = _uiState.value.query.trim()

        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Nhập email người dùng cần tìm"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                users = emptyList()
            )

            try {
                val currentUserId = authRepository.getCurrentUser()?.uid

                val users = userRepository.searchUsersByEmail(email)
                    .filter { it.id != currentUserId }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    users = users,
                    errorMessage = if (users.isEmpty()) "Không tìm thấy người dùng" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Tìm kiếm thất bại"
                )
            }
        }
    }

    fun createDirectConversation(otherUser: User) {
        val currentUser = authRepository.getCurrentUser()

        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Người dùng chưa đăng nhập"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                createdConversationId = null
            )

            try {
                val conversationId = conversationRepository.createDirectConversation(
                    currentUser = currentUser,
                    otherUser = otherUser
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    createdConversationId = conversationId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Tạo cuộc trò chuyện thất bại"
                )
            }
        }
    }

    fun clearCreatedConversation() {
        _uiState.value = _uiState.value.copy(
            createdConversationId = null
        )
    }
}