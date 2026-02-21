package com.example.chattingapp.data.repository

import com.example.chattingapp.data.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()
    fun sendMessage(message: Message, onComplete: (Boolean) -> Unit) {
        firestore.collection("messages")
            .add(message)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
    fun getMessages(): Flow<List<Message>> = callbackFlow {
        val subscription = firestore.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { subscription.remove() }
    }
}