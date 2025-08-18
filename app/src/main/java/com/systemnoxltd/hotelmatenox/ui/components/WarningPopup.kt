package com.systemnoxltd.hotelmatenox.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun WarningPopup(
    title: String = "Warning",
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            Button(onClick = { onConfirm() }) {
                Text("Yes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text("No")
            }
        }
    )
}
