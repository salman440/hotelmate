package com.systemnoxltd.hotelmate.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Activity not found")
}

fun openPlayStore(context: Context) {
    val appPackageName = context.packageName
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$appPackageName")
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // fallback to browser
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
        )
    }
}

fun formatMillisToDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}
