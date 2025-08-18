package com.systemnoxltd.hotelmatenox.ui.screens

import android.app.Activity
import android.app.Application
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.systemnoxltd.hotelmatenox.AdUnits
import com.systemnoxltd.hotelmatenox.ads.InterstitialAdManager
import com.systemnoxltd.hotelmatenox.model.PaymentFilter
import com.systemnoxltd.hotelmatenox.ui.components.AddPaymentDialog
import com.systemnoxltd.hotelmatenox.ui.components.ClientPaymentsCard
import com.systemnoxltd.hotelmatenox.ui.components.PaymentCard
import com.systemnoxltd.hotelmatenox.ui.components.SwipeToDeleteContainer
import com.systemnoxltd.hotelmatenox.ui.components.WarningPopup
import com.systemnoxltd.hotelmatenox.viewmodel.AdsViewModel
import com.systemnoxltd.hotelmatenox.viewmodel.PaymentsViewModel


@Composable
fun PaymentsScreen(
    navController: NavHostController,
    agentId: String,
    clientId: String,
    viewModel: PaymentsViewModel = viewModel()
) {
    val payments by viewModel.payments.collectAsState()
    val pending by viewModel.pendingAmount.collectAsState()
    val received by viewModel.receivedAmount.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    var clientName by remember { mutableStateOf("") }
    var showWarning by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(PaymentFilter.ALL) }
    // Access Activity
//    val activity = LocalContext.current as? Activity
    val activity = LocalActivity.current

    // Ads state
    val adsViewModel: AdsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as Application)
    )
    val showAds by adsViewModel.showAds.collectAsState(initial = true)

    // Interstitial manager
    val interstitialManager = remember { InterstitialAdManager(activity!!) }

    // Pre-load interstitial once when screen loads
    LaunchedEffect(Unit) {
        interstitialManager.load(AdUnits.INTERSTITIAL)
    }

    LaunchedEffect(clientId) {
        viewModel.fetchClientPaymentSummary(clientId) { c ->
            clientName = c
        }
//        viewModel.getPayments(clientId)
        viewModel.getPaymentsByFilter(clientId, selectedFilter)
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
                clientId = clientId,
                pending = pending.toInt(),
                received = received.toInt(),
            )
            Row(
                modifier = Modifier.padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier,
                    text = "Payments History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Button(
                    onClick = { showWarning = true },
                    modifier = Modifier,
                ) {
                    Text(
                        text = "Clear All", fontSize = 12.sp, // smaller font size
                        modifier = Modifier.padding(vertical = 0.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentFilter.values().forEach { filter ->
                    val text = when (filter) {
                        PaymentFilter.ALL -> "All"
                        PaymentFilter.RECEIVED -> "Received"
                        PaymentFilter.CLEARED -> "Cleared"
                    }
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = {
                            selectedFilter = filter
                            viewModel.getPaymentsByFilter(clientId, filter)
                        },
                        label = { Text(text) }
                    )
                }
            }

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

                    // 2. Show interstitial ad after saving (if allowed)
                    if (showAds && activity != null && !activity.isFinishing) {
                        interstitialManager.show(activity) {
                            // Callback when ad is closed
                            interstitialManager.load(AdUnits.INTERSTITIAL) // preload for next use
                        }
                    }
                }
            )
        }

        if (showWarning) {
            WarningPopup(
                message = "Are you sure you want to clear all payments?",
                onConfirm = {
                    viewModel.clearPayments(clientId, { success ->
                        if (success) {
//                            update the payments details and payments history
                            viewModel.fetchClientPaymentSummary(clientId) { c ->
                                clientName = c
                            }
                            viewModel.getPayments(clientId)
                        }
                    })
                    showWarning = false
                },
                onDismiss = { showWarning = false }
            )
        }
    }
}
