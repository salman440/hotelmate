package com.systemnoxltd.hotelmatenox.ui.permissions

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.systemnoxltd.hotelmatenox.utils.hasPostNotificationsPermission
import com.systemnoxltd.hotelmatenox.viewmodel.NotificationViewModel

@Composable
fun RequestNotificationsOnStart(
    notificationVM: NotificationViewModel,
    askOnStart: Boolean = true
) {
    val context = LocalContext.current
    var requested by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // If there was a pending notification, show it now
            notificationVM.tryPostPendingIfPermitted(context)
        } else {
            // user denied – do nothing (you can show a snackbar/toast if you want)
            Toast.makeText(context, "Notifications permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(askOnStart) {
        if (!askOnStart) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasPostNotificationsPermission(context) &&
            !requested
        ) {
            requested = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // expose this launcher to other UI parts by hoisting if you want
}
