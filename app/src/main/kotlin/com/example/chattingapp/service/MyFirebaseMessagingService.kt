package com.example.chattingapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.chattingapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM_TOKEN", "New token: $token")

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val deviceId = token.hashCode().toString()

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
            .set(
                mapOf(
                    "token" to token,
                    "platform" to "ANDROID",
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Tin nhắn mới"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Bạn có tin nhắn mới"

        showNotification(title, body)
    }

    private fun showNotification(
        title: String,
        body: String
    ) {
        val channelId = "chat_messages"
        val manager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat messages",
                NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}