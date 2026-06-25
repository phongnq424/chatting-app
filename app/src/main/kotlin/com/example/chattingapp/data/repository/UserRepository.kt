package com.example.chattingapp.data.repository

import com.example.chattingapp.data.mapper.toDomain
import com.example.chattingapp.data.model.UserDto
import com.example.chattingapp.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getUserById(uid: String): User? {
        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        val dto = snapshot.toObject(UserDto::class.java) ?: return null
        return dto.toDomain()
    }

    suspend fun searchUsersByEmail(email: String): List<User> {
        val snapshot = db.collection("users")
            .whereEqualTo("email", email.trim())
            .limit(10)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(UserDto::class.java)?.toDomain()
        }
    }
}