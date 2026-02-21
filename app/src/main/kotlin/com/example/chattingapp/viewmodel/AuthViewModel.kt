package com.example.chattingapp.viewmodel
import com.example.chattingapp.data.repository.AuthRepository
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    var isLoading = mutableStateOf(false)
    var loginSuccess = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    val currentUser: FirebaseUser?
        get() = repo.getCurrentUser()

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            errorMessage.value = "Vui lòng không để trống!"
            return
        }
        isLoading.value = true
        repo.signInWithEmail(email, pass) { error ->
            isLoading.value = false
            if (error == null) {
                loginSuccess.value = true
                errorMessage.value = null
            } else {
                loginSuccess.value = false
                errorMessage.value = error
            }
        }
    }
    fun signUp(email: String, pass: String, name: String) {
        if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            errorMessage.value = "Vui lòng điền đầy đủ thông tin!"
            return
        }
        isLoading.value = true
        repo.signUpWithEmail(email, pass, name) { error ->
            isLoading.value = false
            if (error == null) {
                loginSuccess.value = true
                errorMessage.value = null
            } else {
                errorMessage.value = error
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        repo.logout()
        onComplete()
    }

    fun onGoogleSignInResult(idToken: String) {
        isLoading.value = true
        repo.signInWithGoogle(idToken) { error ->
            isLoading.value = false
            if (error == null) {
                loginSuccess.value = true
            } else {
                errorMessage.value = error
            }
        }
    }
    fun updateName(newName: String, onSuccess: () -> Unit) {
        if (newName.isBlank()) {
            errorMessage.value = "Tên không được để trống"
            return
        }
        isLoading.value = true
        repo.updateDisplayName(newName) { error ->
            isLoading.value = false
            if (error == null) {
                errorMessage.value = null
                onSuccess() // Tắt chế độ chỉnh sửa
            } else {
                errorMessage.value = error
            }
        }
    }
    fun updateFCMToken(token: String) {
        repo.updateFCMToken(token)
    }
}