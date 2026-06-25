package com.example.chattingapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.chattingapp.core.navigation.AppNavGraph
import com.example.chattingapp.core.navigation.NavRoutes
import com.example.chattingapp.data.repository.AuthRepository
import com.example.chattingapp.data.repository.ConversationRepository
import com.example.chattingapp.data.repository.MessageRepository
import com.example.chattingapp.data.repository.UserRepository
import com.example.chattingapp.ui.theme.ChattingAppTheme
import com.example.chattingapp.viewmodel.AuthViewModel
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepository = AuthRepository()
        val userRepository = UserRepository()
        val conversationRepository = ConversationRepository()
        val messageRepository = MessageRepository()

        val authViewModel = AuthViewModel(authRepository)

        askNotificationPermission()
        updateFcmToken(authViewModel)

        enableEdgeToEdge()

        setContent {
            ChattingAppTheme {
                val navController = rememberNavController()

                val startDestination = if (authRepository.getCurrentUser() != null) {
                    NavRoutes.Conversations
                } else {
                    NavRoutes.Login
                }

                AppNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    authRepository = authRepository,
                    authViewModel = authViewModel,
                    userRepository = userRepository,
                    conversationRepository = conversationRepository,
                    messageRepository = messageRepository
                )
            }
        }
    }

    private fun updateFcmToken(authViewModel: AuthViewModel) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                authViewModel.updateFCMToken(token)
                Log.d("FCM_TOKEN", "Current token: $token")
            } else {
                Log.e("FCM_TOKEN", "Cannot get FCM token", task.exception)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    101
                )
            }
        }
    }
}