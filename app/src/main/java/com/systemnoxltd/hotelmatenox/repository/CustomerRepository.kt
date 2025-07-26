package com.systemnoxltd.hotelmatenox.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.systemnoxltd.hotelmatenox.model.Customer

class CustomerRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("customers")

    fun getCustomersByAgent(agentId: String, onComplete: (List<Customer>) -> Unit) {
        collection.whereEqualTo("agentId", agentId)
            .get()
            .addOnSuccessListener { snapshot ->
                val customers = snapshot.map { it.toObject(Customer::class.java).copy(id = it.id) }
                onComplete(customers)
            }
    }

    fun addCustomer(customer: Customer, onSuccess: () -> Unit) {
        collection.add(customer).addOnSuccessListener { onSuccess() }
    }

    fun updateCustomer(customer: Customer, onSuccess: () -> Unit) {
        collection.document(customer.id).set(customer).addOnSuccessListener { onSuccess() }
    }

    fun deleteCustomer(id: String, onSuccess: () -> Unit) {
        collection.document(id).delete().addOnSuccessListener { onSuccess() }
    }
}
