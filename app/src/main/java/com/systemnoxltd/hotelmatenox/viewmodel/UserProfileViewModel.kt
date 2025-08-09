package com.systemnoxltd.hotelmatenox.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.systemnoxltd.hotelmatenox.model.UserProfile
import com.systemnoxltd.hotelmatenox.repository.UserRepository

class UserProfileViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadUserProfile() {
        isLoading = true
        repository.fetchUserProfile { profile, error ->
            if (profile != null) {
                userProfile = profile
                errorMessage = null
            } else {
                errorMessage = error
            }
            isLoading = false
        }
    }

    fun changePassword(onComplete: (Boolean, String?) -> Unit) {
        repository.sendPasswordResetEmail { success, error ->
            onComplete(success, error)
        }
    }
}
