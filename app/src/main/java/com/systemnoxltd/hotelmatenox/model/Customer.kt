package com.systemnoxltd.hotelmatenox.model

import com.google.firebase.Timestamp

data class Customer(
    val id: String = "",
    val serialNo: String = "",
    val voucherNo: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val checkInDate: Long = 0,
    val checkOutDate: Long = 0,
    val roomType: String = "", // "Sharing" or "Separate"
    val totalRooms: Int = 0,
    val nights: Int = 0,
    val totalNights: Int = 0,
    val rentPerNight: Double = 0.0,
    val totalAmount: Double = 0.0,
    val hotelId: String = "",
    val hotelName: String = "",
    val agentId: String = "", // for filtering by current user
    val clientId: String = "",
    val client: String = "",
    val status: String = "Pending",
)
