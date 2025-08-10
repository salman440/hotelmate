package com.systemnoxltd.hotelmatenox.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.systemnoxltd.hotelmatenox.model.Client

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditClientScreen(
    navController: NavHostController,
    isEdit: Boolean,
    clientId: String = ""
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val agentId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }

    // Load data if editing
    LaunchedEffect(isEdit, clientId) {
        if (isEdit && clientId.isNotEmpty()) {
            db.collection("agents").document(agentId).collection("clients")
                .document(clientId).get().addOnSuccessListener { doc ->
                    doc.toObject(Client::class.java)?.let {
                        name = it.clientName
                        phone = it.clientPhone
                        city = it.clientCity
                        company = it.clientCompany
                    }
                }
        }
    }

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(if (isEdit) "Edit Client" else "Add Client") }
//            )
//        }
    ) { padding ->
        Column(
            modifier = Modifier
//                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Client Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
//                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text("Company") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank() || city.isBlank() || company.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    loading = true
                    val data = mapOf(
                        "clientName" to name,
                        "clientPhone" to phone,
                        "clientCity" to city,
                        "clientCompany" to company,
                        "agentId" to agentId
                    )

                    val collection = db.collection("agents").document(agentId).collection("clients")
                    if (isEdit) {
                        collection.document(clientId).update(data).addOnSuccessListener {
                            loading = false
                            Toast.makeText(context, "Client updated", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }.addOnFailureListener {
                            loading = false
                            Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        collection.add(data).addOnSuccessListener {
                            loading = false
                            Toast.makeText(context, "Client added", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }.addOnFailureListener {
                            loading = false
                            Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "Update Client" else "Add Client")
            }
        }
    }
}
