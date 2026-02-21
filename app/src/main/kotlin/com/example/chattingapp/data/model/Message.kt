package com.example.chattingapp.data.model

data class Message(
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", null, 0L)
}