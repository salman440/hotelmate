package com.systemnoxltd.hotelmatenox.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.systemnoxltd.hotelmatenox.model.UserProfile

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun fetchUserProfile(onResult: (UserProfile?, String?) -> Unit) {
        val uid = getCurrentUserId()
        if (uid == null) {
            onResult(null, "User not logged in")
            return
        }

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profile = UserProfile(
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: getCurrentUserEmail().orEmpty(),
                        phone = doc.getString("phone") ?: ""
                    )
                    onResult(profile, null)
                } else {
                    onResult(null, "Profile not found")
                }
            }
            .addOnFailureListener { e ->
                onResult(null, e.localizedMessage)
            }
    }

    fun sendPasswordResetEmail(onResult: (Boolean, String?) -> Unit) {
        val email = getCurrentUserEmail()
        if (email.isNullOrEmpty()) {
            onResult(false, "No email found")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }
}
