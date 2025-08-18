package com.systemnoxltd.hotelmatenox.ui.components

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.systemnoxltd.hotelmatenox.model.ReportRow
import com.systemnoxltd.hotelmatenox.utils.NotificationHelper
import com.systemnoxltd.hotelmatenox.utils.generateReport
import com.systemnoxltd.hotelmatenox.utils.hasPostNotificationsPermission
import com.systemnoxltd.hotelmatenox.viewmodel.NotificationViewModel
import com.systemnoxltd.hotelmatenox.viewmodel.PaymentsViewModel
import com.systemnoxltd.hotelmatenox.viewmodel.ReportNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream


@Composable
fun ClientPaymentsCard(
    clientName: String,
    clientId: String,
    pending: Int = 0,
    received: Int = 0,
    viewModel: PaymentsViewModel = viewModel(),
    notificationVM: NotificationViewModel = viewModel(),
) {

    val balance = pending - received
    val total = pending.coerceAtLeast(received) // avoid division errors
    val progress = if (total > 0) (received.toFloat() / total) else 0f
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Launcher to request notifications permission when needed (33+)
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // try to post any queued notification
            notificationVM.tryPostPendingIfPermitted(context)
        } else {
            // user denied; optional: toast/snackbar
        }
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
//            .background(Color.White)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {

            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // Client info row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = clientName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Button(onClick = {
                        viewModel.getClientReportData(clientId) { rows ->

                            scope.launch {
                                generateReport(
                                    context = context,
                                    clientName = clientName,
                                    rows = rows,
                                    onStart = { isLoading = true },
                                    onComplete = { isLoading = false },
                                    onSaved = { uri ->
                                        // 2) post/queue notification based on permission
                                        if (hasPostNotificationsPermission(context)) {
                                            NotificationHelper.showReportNotification(
                                                context,
                                                uri
                                            )
                                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            // queue and request permission now
                                            notificationVM.queue(ReportNotification(uri))
                                            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            // Below 33 no runtime permission; should not happen
                                            NotificationHelper.showReportNotification(
                                                context,
                                                uri
                                            )
                                        }

                                    }
                                )
                            }

                        }
                    }) {
                        Text(
                            text = "Get Report", fontSize = 12.sp, // smaller font size
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
                    progress = {
                        progress // directly pass float
                    },
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

        if (isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}