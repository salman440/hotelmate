package com.systemnoxltd.hotelmatenox.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.systemnoxltd.hotelmatenox.ui.components.CustomerCard
import com.systemnoxltd.hotelmatenox.viewmodel.AgentHomeViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.systemnoxltd.hotelmate.utils.openPlayStore
import com.systemnoxltd.hotelmatenox.R
import com.systemnoxltd.hotelmatenox.viewmodel.CustomerViewModel
import androidx.compose.runtime.livedata.observeAsState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentHomeScreen(
    navController: NavHostController,
    agentId: String,
    viewModel: CustomerViewModel = viewModel()
) {
    val context = LocalContext.current
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

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Agent Home") },
                    actions = {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Clients") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("clients")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hotels") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("hotels")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add New Customer") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("add_customer")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Rate Us") },
                                onClick = {
                                    menuExpanded = false
                                    openPlayStore(context)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Help") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("help")
                                }
                            )
                        }
                    }
                )

                // 🔽 Dropdown below top bar
                if (clients.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            AssistChip(
                                onClick = { selectedClient = "" },
                                label = { Text("All") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (selectedClient == "") colorResource(R.color.teal_200)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                        items(clients) { client ->
                            AssistChip(
                                onClick = { selectedClient = client },
                                label = { Text(client) },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (selectedClient == client) FloatingActionButtonDefaults.containerColor
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("add_customer")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(customers, key = { it.id }) { customer ->
                CustomerCard(
                    customer = customer,
                    onEdit = {
                        navController.navigate("edit_customer/${customer.id}")
                    },
                    onDelete = {
                        viewModel.deleteCustomer(customer.id)
//                        refresh the list of customers
                        viewModel.loadCustomers(agentId, if (selectedClient.isBlank()) null else selectedClient)
                    }
                )
            }
        }
    }
}