package com.systemnoxltd.hotelmatenox.model

import com.google.firebase.Timestamp

data class Payment(
    val id: String = "",
    val amount: Double = 0.0,
    val date: Timestamp = Timestamp.now(),
    val isCleared: Boolean = false,
)
