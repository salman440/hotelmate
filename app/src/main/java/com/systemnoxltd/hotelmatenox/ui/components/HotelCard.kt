package com.systemnoxltd.hotelmatenox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.systemnoxltd.hotelmatenox.model.Customer
import com.systemnoxltd.hotelmatenox.model.Hotel
import com.systemnoxltd.hotelmatenox.ui.components.HotelCard

@Composable
fun HotelCard(
    hotel: Hotel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Box(Modifier.padding(0.dp)
            .background(Color.White),) {

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onEdit() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDelete() }
                )
            }
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Hotel,
                        contentDescription = null,
                        Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = hotel.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = hotel.location, style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = hotel.phone,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHotelCard() {
    // Dummy customer data for preview
    val hotel= Hotel(
        name = "Royal premium hotel",
        location = "Ibrahem road Makkah",
        phone = "03336565665",

    )

    MaterialTheme {
        HotelCard(
            hotel = hotel,
            onEdit = {},
            onDelete = {}
        )
    }
}