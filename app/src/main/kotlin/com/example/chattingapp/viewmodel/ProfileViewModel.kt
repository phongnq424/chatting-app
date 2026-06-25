package com.example.chattingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattingapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val user = authRepository.getCurrentUser()

        _uiState.value = ProfileUiState(
            displayName = user?.displayName ?: "",
            email = user?.email ?: "",
            photoUrl = user?.photoUrl?.toString() ?: ""
        )
    }

    fun updateDisplayName(newName: String) {
        if (newName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Tên hiển thị không được để trống"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )

            authRepository.updateDisplayName(newName.trim()) { error ->
                if (error == null) {
                    _uiState.value = _uiState.value.copy(
                        displayName = newName.trim(),
                        isSaving = false,
                        successMessage = "Cập nhật thành công"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error
                    )
                }
            }
        }
    }
}