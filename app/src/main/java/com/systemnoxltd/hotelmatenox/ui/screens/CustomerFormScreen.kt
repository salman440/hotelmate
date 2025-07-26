package com.systemnoxltd.hotelmatenox.ui.screens

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.systemnoxltd.hotelmatenox.model.Client
import com.systemnoxltd.hotelmatenox.model.Customer
import com.systemnoxltd.hotelmatenox.model.Hotel
import com.systemnoxltd.hotelmatenox.viewmodel.CustomerViewModel
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

fun formatMillisToDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(
    navController: NavHostController,
    agentId: String,
    customerId: String = "",
    isEdit: Boolean = false,
    customerToEdit: Customer? = null,
    viewModel: CustomerViewModel = viewModel()
) {
    val firestore = FirebaseFirestore.getInstance()
    var id by remember { mutableStateOf(customerToEdit?.id ?: "") }
    var voucherNo by remember { mutableStateOf(customerToEdit?.voucherNo ?: "") }
    var customerName by remember { mutableStateOf(customerToEdit?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(customerToEdit?.customerPhone ?: "") }
    var roomType by remember { mutableStateOf(customerToEdit?.roomType ?: "Sharing") }
    var totalRoom by remember { mutableStateOf(customerToEdit?.totalRooms?.toString() ?: "") }
    var rentPerNight by remember { mutableStateOf(customerToEdit?.rentPerNight?.toString() ?: "") }
    var hotelName by remember { mutableStateOf(customerToEdit?.hotelName ?: "") }
    var client by remember { mutableStateOf(customerToEdit?.client ?: "") }
    var hotels by remember { mutableStateOf<List<String>>(emptyList()) }
    var clients by remember { mutableStateOf<List<String>>(emptyList()) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    var checkInMillis by remember {
        mutableStateOf(customerToEdit?.checkInDate?.let {
            dateFormatter.parse(
                it
            )?.time
        } ?: 0L)
    }
    var checkOutMillis by remember {
        mutableStateOf(customerToEdit?.checkOutDate?.let {
            dateFormatter.parse(
                it
            )?.time
        } ?: 0L)
    }
    var nights by remember { mutableStateOf(0) }

    LaunchedEffect(checkInMillis, checkOutMillis) {
        if (checkInMillis > 0 && checkOutMillis > checkInMillis) {
            val diff = checkOutMillis - checkInMillis
            nights = (diff / (1000 * 60 * 60 * 24)).toInt()
        } else {
            nights = 0
        }
    }

//    fetching customer data if edit mode is enabled
    LaunchedEffect(customerId) {
        if (isEdit && customerToEdit == null && customerId.isNotBlank()) {
            val doc = firestore
                .collection("customers")
                .document(customerId)
                .get()
                .await()
            doc.toObject(Customer::class.java)?.let {
                // update all state variables here from `it`
                id = it.id
                Log.e("editCustomer", "CustomerFormScreen: customerId: " + it.id)
                voucherNo = it.voucherNo
                customerName = it.customerName
                customerPhone = it.customerPhone
                roomType = it.roomType
                totalRoom = it.totalRooms.toString()
                rentPerNight = it.rentPerNight.toString()
                hotelName = it.hotelName
                client = it.client
                checkInMillis =
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(it.checkInDate)?.time
                        ?: 0
                checkOutMillis =
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(it.checkOutDate)?.time
                        ?: 0
            }
        }
    }


//    fetching list of hotels and clients
    LaunchedEffect(agentId) {


        try {
            val hotelsSnapshot = firestore
                .collection("agents")
                .document(agentId)
                .collection("hotels")
                .get()
                .await()
            val hotelsList = hotelsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Hotel::class.java)
            }
            hotels = hotelsList.map { it.name }

            val clientsSnapshot = firestore
                .collection("agents")
                .document(agentId)
                .collection("clients")
                .get()
                .await()

            val clientsList = clientsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Client::class.java)
            }
            clients = clientsList.map { it.clientName }
        } catch (e: Exception) {
            e.printStackTrace()
            // Optionally show error via Snackbar/Toast
        }
    }

    var totalNights = (totalRoom.toIntOrNull() ?: 0) * nights
    var totalAmount = totalNights * (rentPerNight.toDoubleOrNull() ?: 0.0)

    fun calculateNights(): Unit {
        totalNights = (totalRoom.toIntOrNull() ?: 0) * nights
        totalAmount = totalNights * (rentPerNight.toDoubleOrNull() ?: 0.0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Customer" else "Add Customer") }
            )
        }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = voucherNo,
                onValueChange = { voucherNo = it },
                label = { Text("Voucher No") })
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Customer Name") })
            OutlinedTextField(
                value = customerPhone,
                onValueChange = { customerPhone = it },
                label = { Text("Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Text("Check-In Date")
            OutlinedTextField(
                value = if (checkInMillis > 0) formatMillisToDate(checkInMillis) else "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        DatePickerDialog(context).apply {
                            setOnDateSetListener { _, year, month, day ->
                                val calendar = Calendar.getInstance()
                                calendar.set(year, month, day)
                                checkInMillis = calendar.timeInMillis
                            }
                            show()
                        }
                    },
                enabled = false
            )

            Text("Check-Out Date")
            OutlinedTextField(
                value = if (checkOutMillis > 0) formatMillisToDate(checkOutMillis) else "",
                onValueChange = {
                    calculateNights()
                },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        DatePickerDialog(context).apply {
                            setOnDateSetListener { _, year, month, day ->
                                val calendar = Calendar.getInstance()
                                calendar.set(year, month, day)
                                checkOutMillis = calendar.timeInMillis
                            }
                            show()
                        }
                    },
                enabled = false
            )

            Text("Room Type")
            Row {
                RadioButton(selected = roomType == "Sharing", onClick = { roomType = "Sharing" })
                Text("Sharing", modifier = Modifier.padding(end = 16.dp))
                RadioButton(selected = roomType == "Separate", onClick = { roomType = "Separate" })
                Text("Separate")
            }

            OutlinedTextField(
                value = totalRoom,
                onValueChange = {
                    totalRoom = it
                    calculateNights()
                },
                label = { Text("Total Rooms") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = nights.toString(),
                onValueChange = {},
                label = { Text("Nights") },
                readOnly = true,
                enabled = false
            )

            OutlinedTextField(
                value = rentPerNight,
                onValueChange = { rentPerNight = it },
                label = { Text("Rent Per Night") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Text("Total Nights: $totalNights")
            Text("Total Amount: $totalAmount")

            DropdownSelector("Select Hotel", hotelName, hotels) { hotelName = it }
            DropdownSelector("Select Client", client, clients) { client = it }

            Button(
                onClick = {
                    val customer = Customer(
                        id = id ?: "",
                        voucherNo = voucherNo,
                        customerName = customerName,
                        customerPhone = customerPhone,
                        checkInDate = formatMillisToDate(checkInMillis),
                        checkOutDate = formatMillisToDate(checkOutMillis),
                        roomType = roomType,
                        totalRooms = totalRoom.toIntOrNull() ?: 0,
                        nights = nights,
                        rentPerNight = rentPerNight.toDoubleOrNull() ?: 0.0,
                        totalNights = totalNights,
                        totalAmount = totalAmount,
                        hotelName = hotelName,
                        client = client,
                        agentId = agentId
                    )
//                    if (isEdit) {
//                        viewModel.updateCustomer(customer)
//                    } else {
//                        viewModel.addCustomer(customer)
//                    }
                    if (isEdit) {
                        if (customer.id.isBlank()) {
                            Log.e("UpdateError", "Customer ID is blank, cannot update")
                        } else {
                            viewModel.updateCustomer(customer)
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("refresh_customers", true)
                            Toast.makeText(
                                context,
                                "Customer updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        viewModel.addCustomer(customer)
                        Toast.makeText(context, "Customer added successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true })
    }
}
