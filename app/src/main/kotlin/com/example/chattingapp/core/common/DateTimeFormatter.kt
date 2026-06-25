package com.example.chattingapp.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeFormatter {
    private val chatTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun formatChatTime(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        return chatTimeFormat.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        return dateFormat.format(Date(timestamp))
    }
}