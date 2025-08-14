package com.systemnoxltd.hotelmatenox.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.systemnoxltd.hotelmatenox.ui.components.AddPaymentDialog
import com.systemnoxltd.hotelmatenox.ui.components.ClientPaymentsCard
import com.systemnoxltd.hotelmatenox.ui.components.PaymentCard
import com.systemnoxltd.hotelmatenox.ui.components.SwipeToDeleteContainer
import com.systemnoxltd.hotelmatenox.viewmodel.PaymentsViewModel


@Composable
fun PaymentsScreen(
    navController: NavHostController,
    agentId:String,
    clientId: String,
    viewModel: PaymentsViewModel = viewModel()
) {
    val payments by viewModel.payments.collectAsState()
    val pending by viewModel.pendingAmount.collectAsState()
    val received by viewModel.receivedAmount.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    var clientName by remember { mutableStateOf("") }

    LaunchedEffect(clientId) {
        viewModel.getClientPaymentSummary(agentId, clientId) {c ->
            clientName = c
        }
        viewModel.getPayments(clientId)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog.value = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Payment", tint = Color.White)
            }
        }
    ) { padding ->
        Column {
            ClientPaymentsCard(
                clientName = clientName,
                pending = pending.toInt(),
                received = received.toInt()
            )

            Text(modifier = Modifier.padding(8.dp), text = "Payments History", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(payments, key = { it.id }) { payment ->
                    SwipeToDeleteContainer(
                        item = payment,
                        onDelete = { viewModel.deletePayment(clientId, payment.id) }
                    ) {
                        PaymentCard(payment)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showDialog.value) {
            AddPaymentDialog(
                onDismiss = { showDialog.value = false },
                onSave = { amount ->
                    viewModel.addPayment(clientId, amount)
                    showDialog.value = false
                }
            )
        }
    }
}
