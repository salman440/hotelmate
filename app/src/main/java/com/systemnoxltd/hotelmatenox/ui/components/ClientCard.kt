package com.systemnoxltd.hotelmatenox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationCity
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
import com.systemnoxltd.hotelmatenox.model.Client
import com.systemnoxltd.hotelmatenox.model.Customer
import com.systemnoxltd.hotelmatenox.ui.components.ClientCard
import kotlin.String

@Composable
fun ClientCard(
    client: Client,
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
                        Icons.Default.Person,
                        contentDescription = null,
                        Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = client.clientName,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = client.clientPhone, style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = client.clientCity, style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationCity,
                        contentDescription = null,
                        Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = client.clientCompany, style = MaterialTheme.typography.bodyMedium)
                }


            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewClientCard() {
    // Dummy customer data for preview
    val client = Client(
        clientName = "Khan Baba",
        clientPhone = "65872345",
        clientCity = "Salmiya",
        clientCompany = "Traveler",
        agentId = "asdfadf"
    )

    MaterialTheme {
        ClientCard(
            client = client,
            onEdit = {},
            onDelete = {}
        )
    }
}