package com.example.chattingapp.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattingapp.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    var isLoading = mutableStateOf(false)
    var loginSuccess = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var avatarUrl = mutableStateOf(repo.getCurrentUser()?.photoUrl?.toString() ?: "")

    val currentUser: FirebaseUser?
        get() = repo.getCurrentUser()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessage.value = "Vui lòng không để trống!"
            return
        }

        isLoading.value = true

        repo.signInWithEmail(email.trim(), pass) { error ->
            isLoading.value = false

            if (error == null) {
                syncCurrentUserState()
                loginSuccess.value = true
                errorMessage.value = null
            } else {
                loginSuccess.value = false
                errorMessage.value = error
            }
        }
    }

    fun signUp(email: String, pass: String, name: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            errorMessage.value = "Vui lòng điền đầy đủ thông tin!"
            return
        }

        isLoading.value = true

        repo.signUpWithEmail(
            email = email.trim(),
            pass = pass,
            name = name.trim()
        ) { error ->
            isLoading.value = false

            if (error == null) {
                syncCurrentUserState()
                loginSuccess.value = true
                errorMessage.value = null
            } else {
                loginSuccess.value = false
                errorMessage.value = error
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        isLoading.value = true

        repo.logout {
            isLoading.value = false
            loginSuccess.value = false
            errorMessage.value = null
            avatarUrl.value = ""
            onComplete()
        }
    }

    fun onGoogleSignInResult(idToken: String) {
        isLoading.value = true

        repo.signInWithGoogle(idToken) { error ->
            isLoading.value = false

            if (error == null) {
                syncCurrentUserState()
                loginSuccess.value = true
                errorMessage.value = null
            } else {
                loginSuccess.value = false
                errorMessage.value = error
            }
        }
    }

    fun updateAvatar(imageUri: Uri) {
        isLoading.value = true

        viewModelScope.launch {
            try {
                val newAvatarUrl = repo.updateAvatar(imageUri)
                avatarUrl.value = newAvatarUrl
                errorMessage.value = null
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage ?: "Lỗi cập nhật ảnh đại diện"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun updateName(newName: String, onSuccess: () -> Unit) {
        if (newName.isBlank()) {
            errorMessage.value = "Tên không được để trống"
            return
        }

        isLoading.value = true

        repo.updateDisplayName(newName.trim()) { error ->
            isLoading.value = false

            if (error == null) {
                errorMessage.value = null
                onSuccess()
            } else {
                errorMessage.value = error
            }
        }
    }

    fun updateFCMToken(token: String) {
        repo.updateFCMToken(token)
    }

    fun clearError() {
        errorMessage.value = null
    }

    fun resetLoginSuccess() {
        loginSuccess.value = false
    }

    private fun syncCurrentUserState() {
        avatarUrl.value = repo.getCurrentUser()?.photoUrl?.toString() ?: ""
    }
}