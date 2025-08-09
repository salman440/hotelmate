package com.systemnoxltd.hotelmatenox

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.systemnoxltd.hotelmate.navigation.AppNavHost
import com.systemnoxltd.hotelmate.utils.openPlayStore
import com.systemnoxltd.hotelmatenox.navigation.BottomNavItem
import com.systemnoxltd.hotelmatenox.ui.theme.HotelMateNoxTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent {
            HotelMateNoxTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val packageName = context.packageName
                val appLink = "https://play.google.com/store/apps/details?id=$packageName"
                var menuExpanded by remember { mutableStateOf(false) }
                val currentUser = FirebaseAuth.getInstance().currentUser
                val agentId = currentUser?.uid ?: ""

                val startDestination = if (currentUser != null) {
                    BottomNavItem.Home.route
                } else {
                    "splash"
                }

                val bottomNavItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Clients,
                    BottomNavItem.Hotels
                )


                // Observe current back stack entry to get current route
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Map route to title
                val title = when (currentRoute) {
                    BottomNavItem.Home.route -> "Home"
                    BottomNavItem.Clients.route -> "Clients"
                    BottomNavItem.Hotels.route -> "Your Hotels"
                    "profile" -> "Profile"
                    "help" -> "Help"
                    else -> "App"
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(title) },
                            actions = {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Profile") },
                                        onClick = {
                                            menuExpanded = false
                                            navController.navigate("profile")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Help") },
                                        onClick = {
                                            menuExpanded = false
                                            navController.navigate("help")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share") },
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "Check out this app: $appLink")
                                                type = "text/plain"
                                            }
                                            val chooser = Intent.createChooser(sendIntent, "Share via")
                                            context.startActivity(chooser)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Rate Us") },
                                        onClick = {
                                            menuExpanded = false
                                            openPlayStore(context)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Logout") },
                                        onClick = {
                                            menuExpanded = false
                                            FirebaseAuth.getInstance().signOut()
                                            navController.navigate("login") {
                                                popUpTo("agent_home") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
//                        val currentRoute = navController
//                            .currentBackStackEntryAsState()
//                            .value?.destination?.route

                        if (currentRoute in bottomNavItems.map { it.route }) {
                            NavigationBar {
                                bottomNavItems.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) },
                                        selected = currentRoute == item.route,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(BottomNavItem.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        agentId = agentId,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
