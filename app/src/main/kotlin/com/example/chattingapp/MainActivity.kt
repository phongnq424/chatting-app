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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chattingapp.data.repository.AuthRepository
import com.example.chattingapp.data.repository.ChatRepository
import com.example.chattingapp.ui.screens.chat.ChatScreen
import com.example.chattingapp.ui.screens.login.LoginScreen
import com.example.chattingapp.ui.screens.profile.ProfileScreen
import com.example.chattingapp.ui.theme.ChattingAppTheme
import com.example.chattingapp.viewmodel.AuthViewModel
import com.example.chattingapp.viewmodel.ChatViewModel
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepo = AuthRepository()
        val chatRepo = ChatRepository()
        val authViewModel = AuthViewModel(authRepo)

        // 1. Xin quyền thông báo cho Android 13+
        askNotificationPermission()

        // 2. Lấy FCM Token và cập nhật thông qua ViewModel
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                authViewModel.updateFCMToken(token)
                Log.d("FCM_TOKEN", "Token của bạn: $token") // In ra để test trên Firebase Console
            }
        }

        enableEdgeToEdge()
        setContent {
            ChattingAppTheme {
                val navController = rememberNavController()
                val startPage = if (authRepo.getCurrentUser() != null) "chat" else "login"

                NavHost(
                    navController = navController,
                    startDestination = startPage
                ) {
                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate("chat") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("chat") {
                        val chatViewModel: ChatViewModel = viewModel(
                            factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return ChatViewModel(chatRepo, authRepo) as T
                                }
                            }
                        )

                        ChatScreen(
                            viewModel = chatViewModel,
                            onLogout = {
                                authViewModel.logout {
                                    navController.navigate("login") {
                                        popUpTo("chat") { inclusive = true }
                                    }
                                }
                            },
                            onNavigateToProfile = {
                                navController.navigate("profile")
                            }
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    // Hàm xin quyền thông báo
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }
}