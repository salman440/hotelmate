package com.systemnoxltd.hotelmatenox.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.systemnoxltd.hotelmatenox.model.Hotel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditHotelScreen(
    navController: NavHostController,
    isEdit: Boolean,
    hotelId: String = ""
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val agentId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }

    // Load existing hotel if editing
    LaunchedEffect(isEdit, hotelId) {
        if (isEdit && hotelId.isNotEmpty()) {
            db.collection("agents").document(agentId).collection("hotels")
                .document(hotelId).get().addOnSuccessListener { doc ->
                    doc.toObject(Hotel::class.java)?.let {
                        name = it.name
                        location = it.location
                        phone = it.phone
                    }
                }
        }
    }

    Scaffold(
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Hotel Name *") },
                singleLine = true,
                isError = nameError,
                modifier = Modifier.fillMaxWidth()
            )
            if (nameError) {
                Text("Hotel name is required", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it
                                locationError = false},
                label = { Text("Location *") },
                singleLine = true,
                isError = locationError,
                modifier = Modifier.fillMaxWidth()
            )
            if (locationError) {
                Text("Location is required", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it
                                phoneError = false},
                label = { Text("Phone *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError,
                modifier = Modifier.fillMaxWidth()
            )
            if (phoneError) {
                Text("Phone number is required", color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    // Validation
                    var valid = true
                    if (name.isBlank()) {
                        nameError = true
                        valid = false
                    }
                    if (location.isBlank()) {
                        locationError = true
                        valid = false
                    }
                    if (phone.isBlank()) {
                        phoneError = true
                        valid = false
                    }

                    if (!valid) return@Button

                    loading = true
                    val data = mapOf(
                        "id" to hotelId,
                        "name" to name,
                        "location" to location,
                        "phone" to phone,
                        "agentId" to agentId
                    )

                    val collection = db.collection("agents").document(agentId).collection("hotels")
                    if (isEdit) {
                        collection.document(hotelId).update(data).addOnSuccessListener {
                            loading = false
                            Toast.makeText(context, "Hotel updated", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    } else {

                        val newDocRef = collection.document() // generates ID
                        val dataWithId =
                            data.toMutableMap().apply { put("id", newDocRef.id) } // add id
                        newDocRef.set(dataWithId).addOnSuccessListener {
                            loading = false
                            Toast.makeText(context, "Hotel added", Toast.LENGTH_SHORT).show()
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
                Text(if (isEdit) "Update Hotel" else "Add Hotel")
            }
        }
    }
}
