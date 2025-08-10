package com.systemnoxltd.hotelmatenox.model

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val showAds: Boolean = true,
)
