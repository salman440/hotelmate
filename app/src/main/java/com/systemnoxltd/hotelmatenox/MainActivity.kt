package com.systemnoxltd.hotelmatenox

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.systemnoxltd.hotelmatenox.navigation.AppNavHost
import com.systemnoxltd.hotelmatenox.utils.openPlayStore
import com.systemnoxltd.hotelmatenox.ads.InterstitialAdManager
import com.systemnoxltd.hotelmatenox.navigation.BottomNavItem
import com.systemnoxltd.hotelmatenox.ui.components.BannerAdView
import com.systemnoxltd.hotelmatenox.ui.theme.HotelMateNoxTheme
import com.systemnoxltd.hotelmatenox.viewmodel.AdsViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.systemnoxltd.hotelmatenox.ui.permissions.RequestNotificationsOnStart
import com.systemnoxltd.hotelmatenox.viewmodel.NotificationViewModel

//class MainActivity : ComponentActivity() {
class MainActivity : AppCompatActivity() {

    private lateinit var adsViewModel: AdsViewModel
    private lateinit var interstitialManager: InterstitialAdManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        adsViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(AdsViewModel::class.java)

        // AppOpenAdManager in Application; set its shouldShow flag based on Firestore value
        val app = application as MyApplication
        lifecycleScope.launchWhenStarted {
            adsViewModel.showAds.collectLatest { show ->
                app.appOpenAdManager.setShouldShowAds(show)
            }
        }

        interstitialManager = InterstitialAdManager(this)
        interstitialManager.load(AdUnits.INTERSTITIAL)


        setContent {
            HotelMateNoxTheme {

                val notificationVM: NotificationViewModel = viewModel()
                RequestNotificationsOnStart(notificationVM = notificationVM, askOnStart = true)

                val navController = rememberNavController()

                val statusBarColor = MaterialTheme.colorScheme.primary

                // Change status bar color & icon color
                LaunchedEffect(statusBarColor) {
                    window.statusBarColor = statusBarColor.toArgb()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        window.insetsController?.setSystemBarsAppearance(
                            0, // 0 = disable light icons → icons are white
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        window.decorView.systemUiVisibility = 0 // clear LIGHT_STATUS_BAR flag
                    }
                }

                val showAds by adsViewModel.showAds.collectAsState()
                val context = LocalContext.current
                val activity = LocalActivity.current
                val packageName = context.packageName
                val appLink = "https://play.google.com/store/apps/details?id=$packageName"
                val currentUser = FirebaseAuth.getInstance().currentUser
                val agentId = currentUser?.uid ?: ""

                // lambda to navigate but show interstitial first when ad is loaded and showAds==true
                val navigateWithInterstitial: (String) -> Unit = { destination ->
//                    val act = activity ?: return@navigateWithInterstitial

                    if (showAds && !activity?.isFinishing!!) {
                        interstitialManager.show(activity) {
                            navController.navigate(destination)
                        }
                    } else {
                        navController.navigate(destination)
                    }
                }

//                val startDestination = if (currentUser != null && currentUser.isEmailVerified) {
//                    BottomNavItem.Home.route
//                } else {
//                    "splash"
//                }
                val startDestination = "splash"

                val bottomNavItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Clients,
                    BottomNavItem.Hotels
                )

// Define your root and auth screens
                val rootScreens = listOf(
                    BottomNavItem.Home.route,
                    BottomNavItem.Clients.route,
                    BottomNavItem.Hotels.route
                )

                val authScreens = listOf(
                    "splash",
                    "login",
                    "signup",
                    "forgot_password",
                    "email_verification"
                )

// Get current route
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

// Decide if we show the TopAppBar
                val showTopBar = currentRoute != null && currentRoute !in authScreens

// Title mapping (optional, you can also get from BottomNavItem label)
                val title = when (currentRoute) {
                    BottomNavItem.Home.route -> "Home"
                    BottomNavItem.Clients.route -> "Clients"
                    BottomNavItem.Hotels.route -> "Your Hotels"
                    "profile" -> "Profile"
                    "help" -> "Help"
                    "add_customer" -> "Add Customer"
                    "edit_customer/{customerId}" -> "Edit Customer"
                    "add_client" -> "Add Client"
                    "edit_client/{id}" -> "Edit Client"
                    "add_hotel" -> "Add Hotel"
                    "edit_hotel/{id}" -> "Edit Hotel"
                    "payments_screen/{clientId}" -> "Payments"
                    else -> "HotelMate" // default
                }

                // Determine if back arrow should show
                val showBackArrow = currentRoute != null && currentRoute !in rootScreens

                Scaffold(
                    topBar = {
                        if (showTopBar) {
                            TopAppBar(
                                title = {
                                    Text(
                                        title,
                                        color = Color.White
                                    )
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    titleContentColor = Color.White, // Optional, ensures title text color
                                    navigationIconContentColor = Color.White, // Optional, ensures icon color
                                    actionIconContentColor = Color.White // For action icons if any
                                ),
                                navigationIcon = {
                                    if (showBackArrow) {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(
                                                Icons.Default.ArrowBackIosNew,
                                                contentDescription = "Back",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    if (!showBackArrow) { // show menu only on root screens
                                        var menuExpanded by remember { mutableStateOf(false) }
                                        IconButton(onClick = { menuExpanded = true }) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = "Menu"
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = menuExpanded,
                                            onDismissRequest = { menuExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Profile") },
                                                onClick = {
                                                    menuExpanded = false
//                                                    navController.navigate("profile")
                                                    navigateWithInterstitial("profile")
                                                }
                                            )
//                                            DropdownMenuItem(
//                                                text = { Text("Help") },
//                                                onClick = {
//                                                    menuExpanded = false
//                                                    navigateWithInterstitial("help")
//                                                }
//                                            )
                                            DropdownMenuItem(
                                                text = { Text("Share") },
                                                onClick = {
                                                    val sendIntent = Intent().apply {
                                                        action = Intent.ACTION_SEND
                                                        putExtra(
                                                            Intent.EXTRA_TEXT,
                                                            "Check out this app: $appLink"
                                                        )
                                                        type = "text/plain"
                                                    }
                                                    val chooser = Intent.createChooser(
                                                        sendIntent,
                                                        "Share via"
                                                    )
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
                                                text = { Text("Privacy Policy") },
                                                onClick = {
                                                    menuExpanded = false
                                                    val url = getString(R.string.privacy_policy)
                                                    val intent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        url.toUri()
                                                    )
                                                    context.startActivity(intent)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Logout") },
                                                onClick = {
                                                    menuExpanded = false
                                                    FirebaseAuth.getInstance().signOut()
                                                    navController.navigate("login") {
                                                        popUpTo("login") { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
//                                .padding(WindowInsets.navigationBars.asPaddingValues())
                        ) {
                            if (showAds && currentRoute !in authScreens) {
                                BannerAdView(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()

                                        .then(
                                            if (currentRoute !in bottomNavItems.map { it.route })
                                                Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
                                            else Modifier
                                        )
                                )
                            }
                            if (currentRoute in bottomNavItems.map { it.route }) {
                                NavigationBar {
                                    bottomNavItems.forEach { item ->
                                        NavigationBarItem(
                                            icon = {
                                                Icon(
                                                    item.icon,
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = { Text(item.label) },
                                            selected = currentRoute == item.route,
                                            onClick = {
                                                navController.navigate(item.route) {
                                                    popUpTo(BottomNavItem.Home.route) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        agentId = agentId,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding),
                        navigateWithInterstitial = navigateWithInterstitial
                    )
                }
            }
        }
    }
}
