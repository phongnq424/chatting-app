package com.example.chattingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattingapp.data.model.Message
import com.example.chattingapp.data.repository.ChatRepository
import com.example.chattingapp.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepo: ChatRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    init {
        listenForMessages()
    }

    private fun listenForMessages() {
        viewModelScope.launch {
            // Dùng repo được truyền vào
            chatRepo.getMessages().collect {
                _messages.value = it
            }
        }
    }

    fun sendMessage(text: String) {
        val user = authRepo.getCurrentUser()

        if (user != null && text.isNotBlank()) {
            val newMessage = Message(
                senderId = user.uid,
                senderName = user.displayName ?: "Ẩn danh",
                text = text,
                timestamp = System.currentTimeMillis()
            )
            chatRepo.sendMessage(newMessage) { /* Handle error */ }
        }
    }
}