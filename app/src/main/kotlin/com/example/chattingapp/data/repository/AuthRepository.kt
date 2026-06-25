package com.example.chattingapp.data.repository

import com.example.chattingapp.data.model.UserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }

                    auth.currentUser
                        ?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {
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

    fun logout(onComplete: () -> Unit = {}) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            auth.signOut()
            onComplete()
            return
        }

        db.collection("users").document(uid)
            .set(
                mapOf(
                    "isOnline" to false,
                    "lastSeenAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .addOnCompleteListener {
                auth.signOut()
                onComplete()
            }
    }

    private fun saveUserToFirestore() {
        val currentUser = auth.currentUser ?: return

        val user = UserDto(
            uid = currentUser.uid,
            email = currentUser.email ?: "",
            displayName = currentUser.displayName ?: "User ${currentUser.uid.take(4)}",
            photoUrl = currentUser.photoUrl?.toString() ?: ""
        )

        db.collection("users")
            .document(user.uid)
            .set(
                mapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl,
                    "isOnline" to true,
                    "lastSeenAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
    }

    fun updateDisplayName(newName: String, onResult: (String?) -> Unit) {
        val user = auth.currentUser

        if (user == null) {
            onResult("Người dùng chưa đăng nhập")
            return
        }

        val profileUpdates = userProfileChangeRequest {
            displayName = newName
        }

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                db.collection("users")
                    .document(user.uid)
                    .set(
                        mapOf(
                            "displayName" to newName,
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()
                    )
                    .addOnCompleteListener { firestoreTask ->
                        if (firestoreTask.isSuccessful) {
                            onResult(null)
                        } else {
                            onResult("Lỗi cập nhật dữ liệu Firestore")
                        }
                    }
            } else {
                onResult(task.exception?.localizedMessage ?: "Lỗi cập nhật Profile")
            }
        }
    }

    fun updateFCMToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        val deviceId = token.hashCode().toString()

        val deviceData = mapOf(
            "token" to token,
            "platform" to "ANDROID",
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val userRef = db.collection("users").document(uid)

        userRef.set(
            mapOf(
                "fcmToken" to token,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )

        userRef.collection("devices")
            .document(deviceId)
            .set(deviceData, SetOptions.merge())
    }
}