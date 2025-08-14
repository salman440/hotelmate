package com.systemnoxltd.hotelmatenox.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.systemnoxltd.hotelmatenox.model.Client
import com.systemnoxltd.hotelmatenox.model.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class CustomerViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers
    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients

    fun loadCustomers(agentId: String, clientFilter: String? = null) {
        viewModelScope.launch {
            try {
                val query = db
                    .collection("customers")
                    .whereEqualTo("agentId", agentId)

                val snapshot = query.get().await()

                val allCustomers = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Customer::class.java)?.copy(id = doc.id)
                }

                _customers.value = if (clientFilter.isNullOrBlank()) {
                    allCustomers.sortedByDescending { it.client } // if `createdAt` exists
                } else {
                    allCustomers.filter { it.clientId == clientFilter }
                }

            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Failed to load customers", e)
            }
        }
    }
//    fun onDateRangeSelected(startDate: Long, endDate: Long, agentId: String, clientFilter: String? = null) {
    fun loadCustomers(startDate: Long, endDate: Long, agentId: String, clientFilter: String? = null) {
        viewModelScope.launch {
            try {
                val query = db
                    .collection("customers")
                    .whereEqualTo("agentId", agentId)
                    .whereGreaterThanOrEqualTo("checkInDate", startDate)
                    .whereLessThanOrEqualTo("checkInDate", endDate)

                val snapshot = query.get().await()

                val allCustomers = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Customer::class.java)?.copy(id = doc.id)
                }

                _customers.value = if (clientFilter.isNullOrBlank()) {
                    allCustomers.sortedByDescending { it.checkInDate  } // if `createdAt` exists
                } else {
                    allCustomers.filter { it.clientId == clientFilter }
                        .sortedByDescending { it.checkInDate }
                }
                Log.e("FilteredCustomer: ","${_customers.value}")

            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Failed to load customers", e)
            }
        }
    }

    fun loadClients(agentId: String) {
        viewModelScope.launch {
            try {
                val snapshot = db
                    .collection("agents")
                    .document(agentId)
                    .collection("clients")
                    .whereEqualTo("agentId", agentId)
                    .get()
                    .await()

                _clients.value = snapshot.documents.mapNotNull {doc ->
//                    it.getString("clientName")
                    doc.toObject(Client::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Failed to load clients", e)
            }
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            try {
                db.collection("customers")
                    .document(customerId)
                    .delete()
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Delete failed", e)
            }
        }
    }

    fun addCustomer(customer: Customer) {
        val doc = db.collection("customers").document()
        val newCustomer = customer.copy(id = doc.id)
        doc.set(newCustomer)
    }

    fun updateCustomer(customer: Customer) {
        db.collection("customers").document(customer.id).set(customer)
    }


}