package com.example.chattingapp.domain.model

data class Attachment(
    val url: String,
    val type: AttachmentType,
    val fileName: String = "",
    val sizeBytes: Long = 0L
)

enum class AttachmentType {
    IMAGE,
    VIDEO,
    FILE
}