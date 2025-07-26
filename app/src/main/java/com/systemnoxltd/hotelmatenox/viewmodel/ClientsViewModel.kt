package com.systemnoxltd.hotelmatenox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.systemnoxltd.hotelmatenox.model.Client
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClientsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val agentId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients

    fun loadClients() {
        db.collection("agents").document(agentId).collection("clients")
            .addSnapshotListener { snapshot, _ ->
                val result = snapshot?.documents?.map { doc ->
                    doc.toObject(Client::class.java)?.copy(id = doc.id)
                }?.filterNotNull().orEmpty()

                _clients.value = result
            }
    }

    fun deleteClient(id: String) {
        db.collection("agents").document(agentId)
            .collection("clients").document(id).delete()
    }
}
