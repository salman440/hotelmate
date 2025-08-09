package com.systemnoxltd.hotelmatenox.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("agent_home", Icons.Default.Home, "Home")
    object Clients : BottomNavItem("clients", Icons.Default.Person, "Clients")
    object Hotels : BottomNavItem("hotels", Icons.Default.Hotel, "Hotels")
}
