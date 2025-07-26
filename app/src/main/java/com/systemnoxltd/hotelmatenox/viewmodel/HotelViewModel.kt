package com.systemnoxltd.hotelmatenox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.systemnoxltd.hotelmatenox.model.Hotel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HotelViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val agentId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private val _hotels = MutableStateFlow<List<Hotel>>(emptyList())
    val hotels: StateFlow<List<Hotel>> = _hotels

    fun loadHotels() {
        db.collection("agents").document(agentId).collection("hotels")
            .addSnapshotListener { snapshots, _ ->
                val list = mutableListOf<Hotel>()
                snapshots?.forEach { doc ->
                    val hotel = doc.toObject(Hotel::class.java).copy(id = doc.id)
                    list.add(hotel)
                }
                _hotels.value = list
            }
    }

    fun deleteHotel(hotelId: String) {
        db.collection("agents").document(agentId).collection("hotels")
            .document(hotelId).delete()
    }
}
