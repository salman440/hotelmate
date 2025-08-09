package com.systemnoxltd.hotelmatenox.ui.screens

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.systemnoxltd.hotelmate.utils.formatMillisToDate
import com.systemnoxltd.hotelmatenox.model.Client
import com.systemnoxltd.hotelmatenox.model.Customer
import com.systemnoxltd.hotelmatenox.model.Hotel
import com.systemnoxltd.hotelmatenox.viewmodel.CustomerViewModel
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

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

    var voucherNoError by remember { mutableStateOf(false) }
    var customerNameError by remember { mutableStateOf(false) }
    var customerPhoneError by remember { mutableStateOf(false) }
    var totalRoomError by remember { mutableStateOf(false) }
    var rentPerNightError by remember { mutableStateOf(false) }
    var checkInError by remember { mutableStateOf(false) }
    var checkOutError by remember { mutableStateOf(false) }
    var hotelError by remember { mutableStateOf(false) }
    var clientError by remember { mutableStateOf(false) }


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
            val doc = firestore.collection("customers").document(customerId).get().await()
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
            val hotelsSnapshot =
                firestore.collection("agents").document(agentId).collection("hotels").get().await()
            val hotelsList = hotelsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Hotel::class.java)
            }
            hotels = hotelsList.map { it.name }

            val clientsSnapshot =
                firestore.collection("agents").document(agentId).collection("clients").get().await()

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
                title = { Text(if (isEdit) "Edit Customer" else "Add Customer") })
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
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true,
                value = voucherNo,
                onValueChange = {
                    voucherNo = it
                    voucherNoError = false
                },
                label = { Text("Voucher No") },
                isError = voucherNoError,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true,
                value = customerName,
                onValueChange = {
                    customerName = it
                    customerNameError = false
                },
                label = { Text("Customer Name") },
                isError = customerNameError,

                )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true,
                value = customerPhone,
                onValueChange = {
                    customerPhone = it
                    customerPhoneError = false
                },
                label = { Text("Phone") },
                isError = customerPhoneError,
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

                                // Optionally reset checkOut if it's now invalid
                                if (checkOutMillis < calendar.timeInMillis) {
                                    checkOutMillis = 0L
                                }
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
                        if (checkInMillis <= 0) {
                            Toast.makeText(
                                context, "Please select check-in date first.", Toast.LENGTH_SHORT
                            ).show()
                            return@clickable
                        }

                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = checkInMillis

                        DatePickerDialog(context).apply {
                            setOnDateSetListener { _, year, month, day ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(year, month, day)

                                if (selectedCalendar.timeInMillis >= checkInMillis) {
                                    checkOutMillis = selectedCalendar.timeInMillis
                                    calculateNights()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Check-out date cannot be before check-in date.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            datePicker.minDate = checkInMillis
                            show()
                        }
                    },
                enabled = false
            )


            Text("Room Type")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = roomType == "Sharing", onClick = { roomType = "Sharing" })
                Text("Sharing", modifier = Modifier.padding(end = 16.dp))
                RadioButton(selected = roomType == "Separate", onClick = { roomType = "Separate" })
                Text("Separate")
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true,
                value = totalRoom,
                onValueChange = {
                    totalRoom = it
                    totalRoomError = false
                    calculateNights()
                },
                label = { Text("Total Rooms") },
                isError = totalRoomError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = nights.toString(),
                onValueChange = {},
                label = { Text("Nights") },
                readOnly = true,
                enabled = false
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true,
                value = rentPerNight,
                onValueChange = {
                    rentPerNight = it
                    rentPerNightError = false
                },
                label = { Text("Rent Per Night") },
                isError = rentPerNightError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

//            Text("Total Nights: $totalNights")
//            Text("Total Amount: $totalAmount")

            // Total Nights & Total Amount Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InfoCard(
                    Icons.Default.NightsStay,
                    "Total Nights",
                    totalNights.toString(),
                    Color(0xFF3F51B5)
                )
                InfoCard(
                    Icons.Default.AttachMoney, "Total Amount", "Rs. $totalAmount", Color(0xFF388E3C)
                )
            }

            DropdownSelector("Select Hotel", hotelName, hotels) {
                hotelName = it
                hotelError = it.isBlank()
            }
            if (hotelError) Text("Please select a hotel", color = MaterialTheme.colorScheme.error)
            DropdownSelector("Select Client", client, clients) {
                client = it
                clientError = it.isBlank()
            }
            if (clientError) Text("Please select a client", color = MaterialTheme.colorScheme.error)

            Button(
                onClick = {

                    val isValid = voucherNo.isNotBlank()
                        .also { voucherNoError = !it } && customerName.isNotBlank()
                        .also { customerNameError = !it } && customerPhone.isNotBlank()
                        .also { customerPhoneError = !it } &&
//                            totalRoom.toIntOrNull() != null.also { totalRoomError = !it } &&
//                            rentPerNight.toDoubleOrNull() != null.also { rentPerNightError = !it } &&
                            hotelName.isNotBlank().also { hotelError = !it } && client.isNotBlank()
                        .also {
                            clientError = !it
                        } && checkInMillis > 0 && checkOutMillis > checkInMillis

                    if (!isValid) {
                        Toast.makeText(
                            context, "Please fix the errors before saving.", Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

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

                    if (isEdit) {
                        if (customer.id.isBlank()) {
                            Log.e("UpdateError", "Customer ID is blank, cannot update")
                        } else {
                            viewModel.updateCustomer(customer)
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "refresh_customers",
                                    true
                                )
                            Toast.makeText(
                                context, "Customer updated successfully", Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        viewModel.addCustomer(customer)
                        Toast.makeText(context, "Customer added successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                    navController.popBackStack()
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun InfoCard(icon: ImageVector, title: String, value: String, iconColor: Color) {
    Card(
        modifier = Modifier
//            .weight(1f)
            .padding(4.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconColor)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium)
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = Int.MAX_VALUE,
                    softWrap = true
                )
            }
        }
    }
}

@Composable
fun DropdownSelector(
    label: String, selected: String, options: List<String>, onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown Arrow"
                )
            })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onSelected(it)
                    expanded = false
                })
            }
        }
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true })
    }
}
