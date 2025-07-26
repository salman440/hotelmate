package com.systemnoxltd.hotelmatenox.model

data class Customer(
    val id: String = "",
    val serialNo: String = "",
    val voucherNo: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val roomType: String = "", // "Sharing" or "Separate"
    val totalRooms: Int = 0,
    val nights: Int = 0,
    val totalNights: Int = 0,
    val rentPerNight: Double = 0.0,
    val totalAmount: Double = 0.0,
    val hotelName: String = "",
    val agentId: String = "", // for filtering by current user
    val client: String = "",
)
