package com.systemnoxltd.hotelmatenox.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.systemnoxltd.hotelmate.utils.openPlayStore
import com.systemnoxltd.hotelmatenox.ui.components.CustomerCard
import com.systemnoxltd.hotelmatenox.viewmodel.CustomerViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentHomeScreen(
    navController: NavHostController,
    agentId: String,
    viewModel: CustomerViewModel = viewModel()
) {
    val context = LocalContext.current
    val packageName = context.packageName
    val appLink = "https://play.google.com/store/apps/details?id=$packageName"

    val customers by viewModel.customers.collectAsState()
    val clients by viewModel.clients.collectAsState()

    var selectedClient by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    // Load initial customers and clients
    LaunchedEffect(agentId) {
        viewModel.loadCustomers(agentId)
        viewModel.loadClients(agentId)
    }

    // Reload customers when client filter changes
    LaunchedEffect(selectedClient) {
        viewModel.loadCustomers(agentId, if (selectedClient.isBlank()) null else selectedClient)
    }

    val shouldRefresh = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("refresh_customers")
        ?.observeAsState()

    LaunchedEffect(shouldRefresh?.value) {
        if (shouldRefresh?.value == true) {
            viewModel.loadCustomers(agentId, if (selectedClient.isBlank()) null else selectedClient)
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh_customers", false)
        }
    }

    Scaffold(modifier = Modifier.fillMaxHeight()
        .background(Color.White),
        topBar = {

        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("add_customer")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column {
            Column {
                // 🔽 horizontal client chips for filter
                if (clients.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedClient == "",
                                onClick = { selectedClient = "" },
                                modifier = Modifier.padding(end = 8.dp)
                                    .border(BorderStroke(0.dp, Color.Transparent)),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (selectedClient == "") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        items(clients) { client ->
                            FilterChip(
                                selected = selectedClient == client,
                                onClick = { selectedClient = client },
                                modifier = Modifier.padding(end = 8.dp)
                                    .border(BorderStroke(0.dp, Color.Transparent)),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (selectedClient == client) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = { Text(client) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
            LazyColumn {
                items(customers, key = { it.id }) { customer ->
                    CustomerCard(
                        customer = customer,
                        onEdit = {
                            navController.navigate("edit_customer/${customer.id}")
                        },
                        onDelete = {
                            viewModel.deleteCustomer(customer.id)
//                        refresh the list of customers
                            viewModel.loadCustomers(
                                agentId,
                                if (selectedClient.isBlank()) null else selectedClient
                            )
                        }
                    )
                }
            }
        }
    }
}