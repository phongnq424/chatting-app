package com.example.chattingapp.ui.common

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember

@Composable
fun rememberDebouncedClick(
    intervalMillis: Long = 700L,
    onClick: () -> Unit
): () -> Unit {
    val lastClickTime = remember { mutableLongStateOf(0L) }

    return {
        val now = SystemClock.elapsedRealtime()

        if (now - lastClickTime.longValue >= intervalMillis) {
            lastClickTime.longValue = now
            onClick()
        }
    }
}