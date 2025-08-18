package com.systemnoxltd.hotelmatenox.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.net.Uri
import android.app.PendingIntent
import android.content.Intent

object NotificationHelper {
    private const val CHANNEL_ID = "report_channel"
    private const val CHANNEL_NAME = "Reports"
    private const val CHANNEL_DESCRIPTION = "Notifications for generated reports"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = CHANNEL_DESCRIPTION }
            mgr.createNotificationChannel(channel)
        }
    }

    /**
     * Safely posts the "report generated" notification.
     * Call this only if hasPostNotificationsPermission(context) == true.
     */
    fun showReportNotification(context: Context, fileUri: Uri) {
        ensureChannel(context)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                fileUri,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save) // use your own small icon if you have one
            .setContentTitle("Report generated")
            .setContentText("Tap to open your report")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), notification)
        } catch (se: SecurityException) {
            // Permission missing or disabled by user; swallow safely.
            se.printStackTrace()
        }
    }
}
