package com.systemnoxltd.hotelmatenox.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.systemnoxltd.hotelmatenox.model.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

class PaymentsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val agentId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private val _pendingAmount = MutableStateFlow<Double>(0.0)
    val pendingAmount: StateFlow<Double> = _pendingAmount
    private val _receivedAmount = MutableStateFlow<Double>(0.0)
    val receivedAmount: StateFlow<Double> = _receivedAmount

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments


    fun getClientPaymentSummary(
        agentId: String,
        clientId: String,
        onResult: (clientName: String) -> Unit
    ) {

        // 1. Get all customers of this client
        db.collection("customers")
            .whereEqualTo("agentId", agentId)
            .whereEqualTo("clientId", clientId)
            .get()
            .addOnSuccessListener { customerSnapshot ->
                val totalPending = customerSnapshot.documents.mapNotNull {
                    it.getDouble("totalAmount") // assuming field is named "pending"
                }.sum()
                _pendingAmount.value = totalPending
// Get client name from first document
                Log.e(
                    "customer:", "getClientPaymentSummary: ${customerSnapshot.documents.firstOrNull()}"
                )
                val clientName =
                    customerSnapshot.documents.firstOrNull()?.getString("client") ?: "Unknown"
                // 2. Get all payments for this client
                db.collection("agents").document(agentId)
                    .collection("clients").document(clientId)
                    .collection("payments")
                    .get()
                    .addOnSuccessListener { paymentsSnapshot ->
                        val totalReceived = paymentsSnapshot.documents.mapNotNull {
                            it.getDouble("amount")
                        }.sum()
                        _receivedAmount.value = totalReceived
                        onResult(clientName)
                    }.addOnFailureListener {
//               error can't find total received amount
                    }
            }.addOnFailureListener {
//               error can't find total pending amount
            }
    }

    fun getPayments(clientId: String) {
        val customerRef = db.collection("customers")
            .whereEqualTo("agentId", agentId)
            .whereEqualTo("clientId", clientId)

        val paymentsRef = db.collection("agents").document(agentId)
            .collection("clients").document(clientId)
            .collection("payments")
            .orderBy("date", Query.Direction.DESCENDING)

        // Listen for payments changes
        paymentsRef.addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                val paymentList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Payment::class.java)?.copy(id = doc.id)
                }
                _payments.value = paymentList

                // Update received amount whenever payments change
                val totalReceived = paymentList.sumOf { it.amount }
                _receivedAmount.value = totalReceived

                // Update pending amount from customers collection
                customerRef.get().addOnSuccessListener { customerSnapshot ->
                    val totalPending = customerSnapshot.documents
                        .mapNotNull { it.getDouble("totalAmount") }
                        .sum()
                    _pendingAmount.value = totalPending
                }
            }
        }
    }


//    fun getPayments(clientId: String) {
//        db.collection("agents").document(agentId).collection("clients").document(clientId)
//            .collection("payments")
//            .orderBy("date", Query.Direction.DESCENDING)
//            .addSnapshotListener { snapshot, e ->
//                if (e == null && snapshot != null) {
//                    _payments.value = snapshot.documents.mapNotNull { doc ->
//                        doc.toObject(Payment::class.java)?.copy(id = doc.id)
//                    }
//                }
//            }
//    }

    fun addPayment(clientId: String, amount: Double) {
        val payment = hashMapOf(
            "amount" to amount,
            "date" to Timestamp.now()
        )
        db.collection("agents").document(agentId).collection("clients").document(clientId)
            .collection("payments").add(payment)
    }

    fun deletePayment(clientId: String, paymentId: String) {
        db.collection("agents").document(agentId).collection("clients").document(clientId)
            .collection("payments").document(paymentId).delete()
    }
}
