package com.systemnoxltd.hotelmatenox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdsViewModel(app: Application) : AndroidViewModel(app) {
    private val _showAds = MutableStateFlow(true) // default true
    val showAds: StateFlow<Boolean> = _showAds.asStateFlow()

    init {
        fetchAdsFlag()
    }

    private fun fetchAdsFlag() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _showAds.value = true
            return
        }
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                _showAds.value = snapshot?.getBoolean("showAds") ?: true
            }
    }

    fun setShowAdsForUser(show: Boolean) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(mapOf("showAds" to show), SetOptions.merge())
    }
}
