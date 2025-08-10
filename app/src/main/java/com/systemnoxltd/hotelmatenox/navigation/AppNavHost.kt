package com.systemnoxltd.hotelmate.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.systemnoxltd.hotelmate.ui.auth.LoginScreen
import com.systemnoxltd.hotelmate.ui.auth.SignUpScreen
import com.systemnoxltd.hotelmate.ui.screens.SplashScreen
import com.systemnoxltd.hotelmatenox.navigation.BottomNavItem
import com.systemnoxltd.hotelmatenox.ui.auth.EmailVerificationScreen
import com.systemnoxltd.hotelmatenox.ui.auth.ForgotPasswordScreen
import com.systemnoxltd.hotelmatenox.ui.screens.AddOrEditClientScreen
import com.systemnoxltd.hotelmatenox.ui.screens.AddOrEditHotelScreen
import com.systemnoxltd.hotelmatenox.ui.screens.AgentHomeScreen
import com.systemnoxltd.hotelmatenox.ui.screens.ClientsScreen
import com.systemnoxltd.hotelmatenox.ui.screens.CustomerFormScreen
import com.systemnoxltd.hotelmatenox.ui.screens.HotelsScreen
import com.systemnoxltd.hotelmatenox.ui.screens.ProfileScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    agentId: String,
    startDestination: String,
    modifier: Modifier = Modifier,
    navigateWithInterstitial: (String) -> Unit = { dest -> navController.navigate(dest) }
) {
    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        // Auth flow
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("forgot_password") { ForgotPasswordScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("email_verification") { EmailVerificationScreen(navController) }

        // Main tabs
        composable(BottomNavItem.Home.route) {
            AgentHomeScreen(navController, agentId = agentId, navigateWithInterstitial = navigateWithInterstitial)
        }
        composable(BottomNavItem.Clients.route) {
            ClientsScreen(navController, navigateWithInterstitial = navigateWithInterstitial)
        }
        composable(BottomNavItem.Hotels.route) {
            HotelsScreen(navController, navigateWithInterstitial = navigateWithInterstitial)
        }

        // Extra screens
        composable("add_customer") {
            CustomerFormScreen(navController, agentId)
        }
        composable("edit_customer/{customerId}") {
            val id = it.arguments?.getString("customerId") ?: ""
            CustomerFormScreen(navController, agentId, id, isEdit = true)
        }
        composable("add_client") {
            AddOrEditClientScreen(navController, isEdit = false)
        }
        composable("edit_client/{id}") {
            val id = it.arguments?.getString("id") ?: ""
            AddOrEditClientScreen(navController, isEdit = true, clientId = id)
        }
        composable("add_hotel") {
            AddOrEditHotelScreen(navController, isEdit = false)
        }
        composable("edit_hotel/{id}") {
            val id = it.arguments?.getString("id") ?: ""
            AddOrEditHotelScreen(navController, isEdit = true, hotelId = id)
        }
        composable("profile") {
            ProfileScreen(navController)
        }

        // Optional placeholders
        composable("trial_expired") { /* TrialExpiredScreen(navController) */ }
        composable("support") { /* SupportScreen(navController) */ }
        composable("notifications") { /* NotificationScreen(navController) */ }
        composable("help") {
//            HelpScreen(navController)
        }
        // Admin
        composable("admin_home") {
//            AdminHomeScreen(navController)
        }
    }
}
