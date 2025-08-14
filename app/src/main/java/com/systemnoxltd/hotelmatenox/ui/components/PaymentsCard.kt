package com.systemnoxltd.hotelmatenox.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PaymentsCard(
    pending: Double = 0.0,
    received: Double = 0.0,
    onViewDetails: () -> Unit = {}
) {

    val balance = pending - received
    val total = pending.coerceAtLeast(received) // avoid division errors
    val progress = if (total > 0) ((received.toFloat() / total).toFloat()) else 0f
    Log.e("Payment", "PaymentsCard: progress: $progress: total: $total")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                    text = "Payment Details",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
                // View Details Button
                Button(
                    onClick = onViewDetails,
                    modifier = Modifier,
//                shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "View Details", fontSize = 12.sp, // smaller font size
                        modifier = Modifier.padding(vertical = 0.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pending", fontWeight = FontWeight.Medium)
                Text("Received", fontWeight = FontWeight.Medium)
                Text("Balance", fontWeight = FontWeight.Medium)
            }

            // Values Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(pending.toString(), color = Color.Red, fontWeight = FontWeight.SemiBold)
                Text(
                    received.toString(),
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    balance.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round,
            )

        }
    }
}
