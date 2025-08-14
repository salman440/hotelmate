package com.systemnoxltd.hotelmatenox.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var cityError by remember { mutableStateOf(false) }
    var companyError by remember { mutableStateOf(false) }

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

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Client Name *") },
                singleLine = true,
                isError = nameError,
                modifier = Modifier.fillMaxWidth()
            )
            if (nameError) {
                Text("Client name is required", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneError = false
                },
                label = { Text("Phone *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError,
                modifier = Modifier.fillMaxWidth()
            )
            if (phoneError) {
                Text("Phone number is required", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // City
            OutlinedTextField(
                value = city,
                onValueChange = {
                    city = it
                    cityError = false
                },
                label = { Text("City *") },
                singleLine = true,
                isError = cityError,
                modifier = Modifier.fillMaxWidth()
            )
            if (cityError) {
                Text("City is required", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Company
            OutlinedTextField(
                value = company,
                onValueChange = {
                    company = it
                    companyError = false
                },
                label = { Text("Company Name *") },
                singleLine = true,
                isError = companyError,
                modifier = Modifier.fillMaxWidth()
            )
            if (companyError) {
                Text("Company name is required", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    // Validation
                    var valid = true
                    if (name.isBlank()) {
                        nameError = true
                        valid = false
                    }
                    if (phone.isBlank()) {
                        phoneError = true
                        valid = false
                    }
                    if (city.isBlank()) {
                        cityError = true
                        valid = false
                    }
                    if (company.isBlank()) {
                        companyError = true
                        valid = false
                    }

                    if (!valid) return@Button

                    loading = true
                    val collection = db.collection("agents").document(agentId).collection("clients")

                    if (isEdit) {
                        val data = mapOf(
                            "id" to clientId,
                            "clientName" to name,
                            "clientPhone" to phone,
                            "clientCity" to city,
                            "clientCompany" to company,
                            "agentId" to agentId
                        )
                        collection.document(clientId).update(data).addOnSuccessListener {
                            loading = false
                            Toast.makeText(context, "Client updated", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }.addOnFailureListener {
                            loading = false
                            Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val newDocRef = collection.document()
                        val dataWithId = mapOf(
                            "id" to newDocRef.id,
                            "clientName" to name,
                            "clientPhone" to phone,
                            "clientCity" to city,
                            "clientCompany" to company,
                            "agentId" to agentId
                        )
                        newDocRef.set(dataWithId).addOnSuccessListener {
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
