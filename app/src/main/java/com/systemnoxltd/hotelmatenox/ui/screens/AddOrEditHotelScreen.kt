package com.systemnoxltd.hotelmatenox.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        topBar = {
            TopAppBar(title = { Text(if (isEdit) "Edit Hotel" else "Add Hotel") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Hotel Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isBlank() || location.isBlank() || phone.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    loading = true
                    val data = mapOf(
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
                        collection.add(data).addOnSuccessListener {
                            loading = false
                            Toast.makeText(context, "Hotel added", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
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
