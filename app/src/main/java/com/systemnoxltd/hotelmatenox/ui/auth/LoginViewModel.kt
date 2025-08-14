package com.systemnoxltd.hotelmatenox.ui.auth

import android.util.Patterns
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState
    var showVerifyDialog = mutableStateOf(false)
    var emailToVerify = mutableStateOf("")
    private var pendingUnverifiedUser: FirebaseUser? = null

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("Please enter a valid email.")
            return
        }

        if (password.length < 8) {
            onError("Password must be at least 8 characters.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {result ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    val user = result.user
                    if (user != null && user.isEmailVerified) {
                        onSuccess()
                    } else {

                        emailToVerify.value = email.trim()
                        pendingUnverifiedUser = user // saving user info for later use to send verification email
                        showVerifyDialog.value = true
                        auth.signOut()
                    }
                }
                .addOnFailureListener {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(it.message ?: "Login failed.")
                }
        }
    }

    fun sendVerificationEmail(
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = pendingUnverifiedUser
        user?.sendEmailVerification()
            ?.addOnSuccessListener {
                showVerifyDialog.value = false
                onSuccess("Verification email sent.")
            }
            ?.addOnFailureListener {
                onFailure("Failed: ${it.message}")
            }
    }


}
