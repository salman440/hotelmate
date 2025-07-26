package com.systemnoxltd.hotelmatenox.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val message: String? = null
)

class ForgotPasswordViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun sendResetLink(onResult: (String) -> Unit) {
        val email = _uiState.value.email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onResult("Please enter a valid email.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onResult("Reset link sent to $email.")
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onResult(it.message ?: "Failed to send reset link.")
            }
    }
}
