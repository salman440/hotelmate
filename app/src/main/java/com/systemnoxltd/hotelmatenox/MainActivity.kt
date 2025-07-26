package com.systemnoxltd.hotelmatenox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.systemnoxltd.hotelmate.navigation.AppNavHost
import com.systemnoxltd.hotelmatenox.ui.theme.HotelMateNoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            HotelMateNoxTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)

            }
        }
    }
}