package com.systemnoxltd.hotelmatenox.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.systemnoxltd.hotelmatenox.model.ReportRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun showAds(): Boolean{
//    if true ads will be enabled else disabled
    return false
}

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
        "market://details?id=$appPackageName".toUri()
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // fallback to browser
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
            )
        )
    }
}

fun formatMillisToDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

// Helper function to get first day of current month timestamp
fun getFirstDayOfCurrentMonth(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

// Helper function to get last day of current month timestamp
fun getLastDayOfCurrentMonth(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.timeInMillis
}

fun formatTimestampToDateString(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd,MMM,yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

suspend fun generateReport(
    context: android.content.Context,
    clientName: String,
    rows: List<ReportRow>,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onSaved: (Uri) -> Unit
) {
    onStart()

    withContext(Dispatchers.IO) {
        try {
            val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
            val sheet = workbook.createSheet("Report")

            val headers = listOf(
                "Voucher No", "Customer Name", "Check-in Date", "Check-out Date", "Room Type",
                "Total Rooms", "Nights", "Total Nights", "Rent per Night", "Amount",
                "Hotel Name", "Payment Date", "Received Payments"
            )

            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { i, title -> headerRow.createCell(i).setCellValue(title) }

            var sumOfAmounts = 0.0
            var sumOfReceived = 0.0

            rows.forEachIndexed { index, row ->
                val excelRow = sheet.createRow(index + 1)
                excelRow.createCell(0).setCellValue(row.voucherNo)
                excelRow.createCell(1).setCellValue(row.customerName)
                excelRow.createCell(2).setCellValue(row.checkInDate)
                excelRow.createCell(3).setCellValue(row.checkOutDate)
                excelRow.createCell(4).setCellValue(row.roomType)
                excelRow.createCell(5).setCellValue(row.totalRooms.toDouble())
                excelRow.createCell(6).setCellValue(row.nights.toDouble())
                excelRow.createCell(7).setCellValue(row.totalNights.toDouble())
                excelRow.createCell(8).setCellValue(row.rentPerNight)
                excelRow.createCell(9).setCellValue(row.amount)
                excelRow.createCell(10).setCellValue(row.hotelName)
                excelRow.createCell(11).setCellValue(row.paymentDate)
                excelRow.createCell(12).setCellValue(row.receivedPayment)

                sumOfAmounts += row.amount
                sumOfReceived += row.receivedPayment
            }

            val totalRow = sheet.createRow(rows.size + 1)
            totalRow.createCell(9).setCellValue("Total: $sumOfAmounts")
            totalRow.createCell(12).setCellValue("Total: $sumOfReceived")

            val balanceRow = sheet.createRow(rows.size + 2)
            balanceRow.createCell(9).setCellValue("Balance: ${sumOfAmounts - sumOfReceived}")

            // Save via MediaStore → Downloads/HotelMateReports
            val fileName = "$clientName.xlsx"
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/HotelMateReports")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Failed to create file URI")

            resolver.openOutputStream(uri).use { out ->
                workbook.write(out)
            }
            workbook.close()

            withContext(Dispatchers.Main) {
                onSaved(uri) // hand off to UI: decide whether to notify now or queue
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        } finally {
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}