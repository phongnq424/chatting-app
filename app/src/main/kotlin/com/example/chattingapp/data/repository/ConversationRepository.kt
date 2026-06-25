package com.example.chattingapp.data.repository

import android.util.Log
import com.example.chattingapp.data.mapper.toDomain
import com.example.chattingapp.data.model.ConversationDto
import com.example.chattingapp.domain.model.Conversation
import com.example.chattingapp.domain.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ConversationRepository {

    private val db = FirebaseFirestore.getInstance()

    fun observeConversations(currentUserId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = db.collection("conversations")
            .whereArrayContains("memberIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ConversationRepository", "observeConversations failed", error)
                    close()

                    return@addSnapshotListener
                }

                val conversations = snapshot
                    ?.documents
                    ?.mapNotNull { document ->
                        document.toObject(ConversationDto::class.java)
                            ?.toDomain(fallbackId = document.id)
                    }
                    ?.sortedByDescending { it.updatedAt }
                    ?: emptyList()

                trySend(conversations)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun createDirectConversation(
        currentUser: FirebaseUser,
        otherUser: User
    ): String {
        val currentUserId = currentUser.uid
        val otherUserId = otherUser.id

        require(currentUserId != otherUserId) {
            "Không thể tạo cuộc trò chuyện với chính mình"
        }

        val conversationId = buildDirectConversationId(currentUserId, otherUserId)
        val conversationRef = db.collection("conversations").document(conversationId)

        val memberIds = listOf(currentUserId, otherUserId).sorted()

        val data = mapOf(
            "id" to conversationId,
            "type" to "DIRECT",
            "title" to "",
            "photoUrl" to "",
            "memberIds" to memberIds,
            "lastMessageText" to "",
            "lastMessageAt" to FieldValue.serverTimestamp(),
            "lastSenderId" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        conversationRef.set(data, SetOptions.merge()).await()

        val currentMemberData = mapOf(
            "userId" to currentUserId,
            "role" to "MEMBER",
            "joinedAt" to FieldValue.serverTimestamp(),
            "lastReadAt" to FieldValue.serverTimestamp(),
            "muted" to false
        )

        val otherMemberData = mapOf(
            "userId" to otherUserId,
            "role" to "MEMBER",
            "joinedAt" to FieldValue.serverTimestamp(),
            "lastReadAt" to FieldValue.serverTimestamp(),
            "muted" to false
        )

        conversationRef.collection("members")
            .document(currentUserId)
            .set(currentMemberData, SetOptions.merge())
            .await()

        conversationRef.collection("members")
            .document(otherUserId)
            .set(otherMemberData, SetOptions.merge())
            .await()

        return conversationId
    }

    suspend fun createGroupConversation(
        title: String,
        owner: FirebaseUser,
        members: List<User>
    ): String {
        val allMemberIds = (members.map { it.id } + owner.uid)
            .distinct()
            .sorted()

        require(title.isNotBlank()) {
            "Tên nhóm không được để trống"
        }

        require(allMemberIds.size >= 2) {
            "Nhóm cần ít nhất 2 thành viên"
        }

        val conversationRef = db.collection("conversations").document()
        val conversationId = conversationRef.id

        val data = mapOf(
            "id" to conversationId,
            "type" to "GROUP",
            "title" to title.trim(),
            "photoUrl" to "",
            "memberIds" to allMemberIds,
            "lastMessageText" to "",
            "lastMessageAt" to FieldValue.serverTimestamp(),
            "lastSenderId" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        conversationRef.set(data).await()

        allMemberIds.forEach { userId ->
            val memberData = mapOf(
                "userId" to userId,
                "role" to if (userId == owner.uid) "OWNER" else "MEMBER",
                "joinedAt" to FieldValue.serverTimestamp(),
                "lastReadAt" to FieldValue.serverTimestamp(),
                "muted" to false
            )

            conversationRef.collection("members")
                .document(userId)
                .set(memberData)
                .await()
        }

        return conversationId
    }

    suspend fun markAsRead(conversationId: String, userId: String) {
        db.collection("conversations")
            .document(conversationId)
            .collection("members")
            .document(userId)
            .set(
                mapOf(
                    "lastReadAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    private fun buildDirectConversationId(userA: String, userB: String): String {
        val sorted = listOf(userA, userB).sorted()
        return "direct_${sorted[0]}_${sorted[1]}"
    }
}