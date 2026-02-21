package com.example.chattingapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.chattingapp.data.model.User
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUser() = auth.currentUser

    fun signInWithEmail(email: String, pass: String, onResult: (String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToFirestore()
                    onResult(null)
                } else {
                    onResult(task.exception?.localizedMessage ?: "Lỗi đăng nhập")
                }
            }
    }

    fun signUpWithEmail(email: String, pass: String, name: String, onResult: (String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val profileUpdates = userProfileChangeRequest { displayName = name }
                    auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                        saveUserToFirestore()
                        onResult(null)
                    }
                } else {
                    onResult(task.exception?.localizedMessage ?: "Lỗi đăng ký")
                }
            }
    }

    fun signInWithGoogle(idToken: String, onResult: (String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToFirestore()
                    onResult(null)
                } else {
                    onResult(task.exception?.localizedMessage ?: "Lỗi xác thực Google")
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    private fun saveUserToFirestore() {
        val currentUser = auth.currentUser ?: return
        val user = User(
            uid = currentUser.uid,
            email = currentUser.email ?: "",
            displayName = currentUser.displayName ?: "User ${currentUser.uid.take(4)}"
        )
        db.collection("users").document(user.uid).set(user)
    }

    fun updateDisplayName(newName: String, onResult: (String?) -> Unit) {
        val user = auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = newName
        }

        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sau khi đổi tên trong Auth thành công, cập nhật luôn trong Firestore
                db.collection("users").document(user.uid)
                    .update("displayName", newName)
                    .addOnCompleteListener { firestoreTask ->
                        if (firestoreTask.isSuccessful) onResult(null)
                        else onResult("Lỗi cập nhật dữ liệu Firestore")
                    }
            } else {
                onResult(task.exception?.localizedMessage ?: "Lỗi cập nhật Profile")
            }
        }
    }
    fun updateFCMToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update("fcmToken", token)
    }
}