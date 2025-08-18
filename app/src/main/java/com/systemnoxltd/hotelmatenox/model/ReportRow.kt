package com.systemnoxltd.hotelmatenox.model

data class ReportRow(
    val voucherNo: String = "",
    val customerName: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val roomType: String = "",
    val totalRooms: Int = 0,
    val nights: Int = 0,
    val totalNights: Int = 0,
    val rentPerNight: Double = 0.0,
    val amount: Double = 0.0,
    val hotelName: String = "",
    val paymentDate: String = "",
    val receivedPayment: Double = 0.0
)
