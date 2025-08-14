package com.systemnoxltd.hotelmatenox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.systemnoxltd.hotelmatenox.model.Client
import com.systemnoxltd.hotelmatenox.ui.components.ClientCard
import com.systemnoxltd.hotelmatenox.viewmodel.ClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    navController: NavHostController,
    viewModel: ClientsViewModel = viewModel(),
    navigateWithInterstitial: (String) -> Unit
) {
    val clients by viewModel.clients.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadClients()
    }

    Scaffold(modifier = Modifier.fillMaxHeight()
        .background(Color.White),
        floatingActionButton = {
            FloatingActionButton(onClick = {
//                navController.navigate("add_client")
                navigateWithInterstitial("add_client")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Client")
            }
        }
    ) { padding ->
        LazyColumn {
            items(clients.size) { index ->
                val client = clients[index]
                ClientCard(
                    client = client,
                    onEdit = {
//                        navController.navigate("edit_client/${client.id}")
                        navigateWithInterstitial("edit_client/${client.id}")
                    },
                    onDelete = {
                        viewModel.deleteClient(client.id)
                    }
                )
            }
        }
    }
}
