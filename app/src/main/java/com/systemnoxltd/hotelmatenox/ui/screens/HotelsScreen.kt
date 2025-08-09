package com.systemnoxltd.hotelmatenox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.systemnoxltd.hotelmatenox.ui.components.HotelCard
import com.systemnoxltd.hotelmatenox.viewmodel.HotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelsScreen(
    navController: NavHostController,
    viewModel: HotelViewModel = viewModel()
) {
    val hotels by viewModel.hotels.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHotels()
    }

    Scaffold(modifier = Modifier.fillMaxHeight()
        .background(Color.White),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("add_hotel")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Hotel")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(hotels.size) { index ->
                val hotel = hotels[index]
                HotelCard(
                    hotel = hotel,
                    onEdit = {
                        navController.navigate("edit_hotel/${hotel.id}")
                    },
                    onDelete = {
                        viewModel.deleteHotel(hotel.id)
                    }
                )
            }
        }
    }
}
