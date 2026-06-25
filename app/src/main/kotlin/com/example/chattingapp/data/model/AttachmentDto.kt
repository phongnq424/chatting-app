package com.example.chattingapp.data.model

data class AttachmentDto(
    val url: String = "",
    val type: String = "",
    val fileName: String = "",
    val sizeBytes: Long = 0L
)