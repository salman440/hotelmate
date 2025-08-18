package com.systemnoxltd.hotelmatenox.viewmodel

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.systemnoxltd.hotelmatenox.model.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.systemnoxltd.hotelmatenox.model.PaymentFilter
import com.systemnoxltd.hotelmatenox.model.ReportRow
import com.systemnoxltd.hotelmatenox.utils.formatTimestampToDateString
import java.sql.Date

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
        clientId: String,
        onResult: (clientName: String) -> Unit
    ) {

        // 1. Get all customers of this client
        db.collection("customers")
            .whereEqualTo("agentId", agentId)
            .whereEqualTo("clientId", clientId)
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener { customerSnapshot ->
                val totalPending = customerSnapshot.documents.mapNotNull {
                    it.getDouble("totalAmount") // assuming field is named "pending"
                }.sum()
                _pendingAmount.value = totalPending
// Get client name from first document
                val clientName =
                    customerSnapshot.documents.firstOrNull()?.getString("client") ?: "Unknown"
                // 2. Get all payments for this client
                db.collection("agents").document(agentId)
                    .collection("clients").document(clientId)
                    .collection("payments")
                    .whereEqualTo("isCleared", false)
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

    fun fetchClientPaymentSummary(
        clientId: String,
        onResult: (clientName: String) -> Unit
    ) {
        val clientRef = db.collection("agents")
            .document(agentId)
            .collection("clients")
            .document(clientId)

        val customersRef = db.collection("customers")
            .whereEqualTo("agentId", agentId)
            .whereEqualTo("clientId", clientId)
            .whereEqualTo("status", "Pending")

        val paymentsRef = clientRef.collection("payments")
            .whereEqualTo("isCleared", false)

        // Step 1: Get client name
        clientRef.get()
            .addOnSuccessListener { clientSnapshot ->
                val clientName = clientSnapshot.getString("clientName") ?: ""

                // Step 2: Get pending amount from customers
                customersRef.get()
                    .addOnSuccessListener { customerSnapshot ->
                        val totalPending = customerSnapshot.documents
                            .mapNotNull { it.getDouble("totalAmount") }
                            .sum()
                        _pendingAmount.value = totalPending

                        // Step 3: Get received amount from payments
                        paymentsRef.get()
                            .addOnSuccessListener { paymentSnapshot ->
                                val totalReceived = paymentSnapshot.documents
                                    .mapNotNull { it.getDouble("amount") }
                                    .sum()
                                _receivedAmount.value = totalReceived

                                // Return all three values
                                onResult(clientName)
                            }
                            .addOnFailureListener {
                                onResult(clientName)
                            }
                    }
                    .addOnFailureListener {
                        onResult(clientName)
                    }
            }
            .addOnFailureListener {
                onResult("Not Found")
            }
    }


    fun getPayments(clientId: String) {
        val customerRef = db.collection("customers")
            .whereEqualTo("agentId", agentId)
            .whereEqualTo("clientId", clientId)
            .whereEqualTo("status", "Pending")

        val paymentsRef = db.collection("agents").document(agentId)
            .collection("clients").document(clientId)
            .collection("payments")
            .whereEqualTo("isCleared", false)
            .orderBy("date", Query.Direction.DESCENDING)

        // Listen for payments changes
        paymentsRef.addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                val paymentList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Payment::class.java)?.copy(id = doc.id)
                }
                _payments.value = paymentList
                Log.e("Payments", "ClientClickedPayments: ${_payments.value}", )
                // Update received amount whenever payments change
                val totalReceived = paymentList.sumOf { it.amount }
                _receivedAmount.value = totalReceived
                Log.e("Payments", "ClientClickedReceivedAmount: ${_receivedAmount.value}", )
                // Update pending amount from customers collection
                customerRef.get().addOnSuccessListener { customerSnapshot ->
                    val totalPending = customerSnapshot.documents
                        .mapNotNull { it.getDouble("totalAmount") }
                        .sum()
                    _pendingAmount.value = totalPending
                    Log.e("Payments", "ClientClickedPendingAmount: ${_pendingAmount.value}", )
                }.addOnFailureListener {
                    _pendingAmount.value = 0.0
                }
            }else{
                Log.e("Payments", "getPayments: ${e?.printStackTrace()}", )
            }
        }
    }

    fun getPaymentsByFilter(clientId: String, filter: PaymentFilter) {
        val customerRef = db.collection("customers")
            .whereEqualTo("agentId", agentId)
            .whereEqualTo("clientId", clientId)
            .whereEqualTo("status", "Pending")

        var paymentsRef = db.collection("agents").document(agentId)
            .collection("clients").document(clientId)
            .collection("payments")
            .orderBy("date", Query.Direction.DESCENDING)

        // Apply filter
        paymentsRef = when (filter) {
            PaymentFilter.ALL -> paymentsRef
            PaymentFilter.RECEIVED -> paymentsRef.whereEqualTo("isCleared", false)
            PaymentFilter.CLEARED -> paymentsRef.whereEqualTo("isCleared", true)
        }

        paymentsRef.addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                val paymentList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Payment::class.java)?.copy(id = doc.id)
                }
                _payments.value = paymentList

                // Update received amount (sum of payments not cleared)
//                val totalReceived = paymentList.filter { !it.isCleared }.sumOf { it.amount }
//                _receivedAmount.value = totalReceived

                // Update pending amount from customers
//                customerRef.get().addOnSuccessListener { customerSnapshot ->
//                    val totalPending = customerSnapshot.documents
//                        .mapNotNull { it.getDouble("totalAmount") }
//                        .sum()
//                    _pendingAmount.value = totalPending
//
//                }.addOnFailureListener {
//                    _pendingAmount.value = 0.0
//                }
            } else {
                Log.e("PaymentsViewModel", "getPaymentsByFilter error: ${e?.printStackTrace()}")
            }
        }
    }


    fun addPayment(clientId: String, amount: Double) {
        val payment = hashMapOf(
            "amount" to amount,
            "date" to Timestamp.now(),
            "isCleared" to false,
        )
        db.collection("agents").document(agentId).collection("clients").document(clientId)
            .collection("payments").add(payment).addOnSuccessListener {
//                updating payments info
                fetchClientPaymentSummary(clientId, {c->})
            }

    }

    fun deletePayment(clientId: String, paymentId: String) {
        db.collection("agents").document(agentId).collection("clients").document(clientId)
            .collection("payments").document(paymentId).delete().addOnSuccessListener {
                //                updating payments info
                fetchClientPaymentSummary(clientId, {c->})
            }
    }

    //    get data for report
    fun getClientReportData(
        clientId: String,
        onResult: (List<ReportRow>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val customersRef = db.collection("customers")
            .whereEqualTo("agentId", agentId)
            .whereEqualTo("clientId", clientId)
            .whereEqualTo("status", "Pending")

        val paymentsRef = db.collection("agents").document(agentId)
            .collection("clients").document(clientId)
            .collection("payments")
            .whereEqualTo("isCleared", false)

        // 1. Fetch customers
        customersRef.get().addOnSuccessListener { customerSnapshot ->
            val customerRows = customerSnapshot.documents.map { doc ->
                ReportRow(
                    voucherNo = doc.getString("voucherNo") ?: "",
                    customerName = doc.getString("customerName") ?: "",
                    checkInDate = doc.getLong("checkInDate")?.let { Date(it).toString() } ?: "",
                    checkOutDate = doc.getLong("checkOutDate")?.let { Date(it).toString() } ?: "",
                    roomType = doc.getString("roomType") ?: "",
                    totalRooms = doc.getLong("totalRooms")?.toInt() ?: 0,
                    nights = doc.getLong("nights")?.toInt() ?: 0,
                    totalNights = doc.getLong("totalNights")?.toInt() ?: 0,
                    rentPerNight = doc.getDouble("rentPerNight") ?: 0.0,
                    amount = doc.getDouble("totalAmount") ?: 0.0,
                    hotelName = doc.getString("hotelName") ?: "",

                    paymentDate = "",
                    receivedPayment = 0.0
                )
            }

            // 2. Fetch payments
            paymentsRef.get().addOnSuccessListener { paymentsSnapshot ->
                val paymentRows = paymentsSnapshot.documents.map { doc ->
                    ReportRow(
                        paymentDate = doc.getTimestamp("date")
                            ?.let { formatTimestampToDateString(it) } ?: "",
                        receivedPayment = doc.getDouble("amount") ?: 0.0
                    )
                }

                // Combine both
                var reportRows: MutableList<ReportRow> = mutableListOf()
                if (customerRows.size >= paymentRows.size) {
                    // Start with customer rows
                    for ((index, customer) in customerRows.withIndex()) {
                        val payment = paymentRows.getOrNull(index) // get payment if exists
                        reportRows.add(
                            customer.copy(
                                paymentDate = payment?.paymentDate ?: "",
                                receivedPayment = payment?.receivedPayment ?: 0.0
                            )
                        )
                    }
                } else {
                    // Start with payment rows
                    for ((index, payment) in paymentRows.withIndex()) {
                        val customer = customerRows.getOrNull(index)
                        reportRows.add(
                            ReportRow(
                                voucherNo = customer?.voucherNo ?: "",
                                customerName = customer?.customerName ?: "",
                                checkInDate = customer?.checkInDate ?: "",
                                checkOutDate = customer?.checkOutDate ?: "",
                                roomType = customer?.roomType ?: "",
                                totalRooms = customer?.totalRooms ?: 0,
                                nights = customer?.nights ?: 0,
                                totalNights = customer?.totalNights ?: 0,
                                rentPerNight = customer?.rentPerNight ?: 0.0,
                                amount = customer?.amount ?: 0.0,
                                hotelName = customer?.hotelName ?: "",
                                paymentDate = payment.paymentDate,
                                receivedPayment = payment.receivedPayment
                            )
                        )
                    }
                }
                onResult(reportRows)

            }.addOnFailureListener {
                onResult(customerRows)
            }

        }.addOnFailureListener {
            onResult(emptyList())
        }
    }

    //    update status of payments from pending to paid and clear all received payments
    fun clearPayments(
        clientId: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        val clientRef = db.collection("agents")
            .document(agentId)
            .collection("clients")
            .document(clientId)

        // Step 1: Update all customers of this client to "Paid"
        db.collection("customers")
            .whereEqualTo("agentId", agentId)
            .whereEqualTo("clientId", clientId)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()

                // update status for all related customers
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "status", "Paid")
                }

                // Step 2: Mark all payments under this client as cleared
                clientRef.collection("payments")
                    .get()
                    .addOnSuccessListener { paymentSnapshot ->
                        for (paymentDoc in paymentSnapshot.documents) {
                            batch.update(paymentDoc.reference, "isCleared", true)
                        }

                        // Commit both customer + payment updates
                        batch.commit()
                            .addOnSuccessListener { onResult(true) }
                            .addOnFailureListener { onResult(false) }
                    }
                    .addOnFailureListener { onResult(false) }
            }
            .addOnFailureListener { onResult(false) }
    }




}
