package com.systemnoxltd.hotelmate.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SignUpViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onConfirmPasswordChanged(confirm: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirm)
    }

    fun signUp(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()
        val confirm = _uiState.value.confirmPassword.trim()

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("Please enter a valid email.")
            return
        }

        if (password.length < 8) {
            onError("Password must be at least 8 characters.")
            return
        }

        if (!password.any { it.isUpperCase() }) {
            onError("Password must contain at least one uppercase letter.")
            return
        }

        if (!password.any { it.isLowerCase() }) {
            onError("Password must contain at least one lowercase letter.")
            return
        }

        if (!password.any { it.isDigit() }) {
            onError("Password must contain at least one number.")
            return
        }

        if (password != confirm) {
            onError("Passwords do not match.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    auth.currentUser?.sendEmailVerification()
                    onSuccess()
                }
                .addOnFailureListener {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(it.message ?: "Sign up failed.")
                }
        }
    }
}
