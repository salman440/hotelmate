package com.systemnoxltd.hotelmate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.systemnoxltd.hotelmate.ui.auth.LoginScreen
import com.systemnoxltd.hotelmate.ui.auth.SignUpScreen
import com.systemnoxltd.hotelmate.ui.screens.SplashScreen
import com.systemnoxltd.hotelmatenox.ui.auth.EmailVerificationScreen
import com.systemnoxltd.hotelmatenox.ui.auth.ForgotPasswordScreen
import com.systemnoxltd.hotelmatenox.ui.screens.AddOrEditClientScreen
import com.systemnoxltd.hotelmatenox.ui.screens.AddOrEditHotelScreen
import com.systemnoxltd.hotelmatenox.ui.screens.AgentHomeScreen
import com.systemnoxltd.hotelmatenox.ui.screens.ClientsScreen
import com.systemnoxltd.hotelmatenox.ui.screens.CustomerFormScreen
import com.systemnoxltd.hotelmatenox.ui.screens.HotelsScreen


@Composable
fun AppNavHost(navController: NavHostController) {

    val currentUser = FirebaseAuth.getInstance().currentUser
    val agentId = currentUser?.uid ?: ""

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController)
        }
        composable("signup") {
            SignUpScreen(navController) // To be implemented next
        }
        composable("email_verification") {
            EmailVerificationScreen(navController)
        }
        // Agent
        composable("agent_home") {
            AgentHomeScreen(navController, agentId = agentId)
        }
        composable("clients") {
            ClientsScreen(navController)
        }
        composable("add_client") {
            AddOrEditClientScreen(navController, isEdit = false)
        }
        composable("edit_client/{id}") {
            val id = it.arguments?.getString("id") ?: ""
            AddOrEditClientScreen(navController, isEdit = true, clientId = id)
        }
        composable("hotels") {
            HotelsScreen(navController)
        }
        composable("add_hotel") {
            AddOrEditHotelScreen(navController, isEdit = false)
        }
        composable("edit_hotel/{id}") {
            val id = it.arguments?.getString("id") ?: ""
            AddOrEditHotelScreen(navController, isEdit = true, hotelId = id)
        }
        composable("help") {
//            HelpScreen(navController)
        }
        composable("add_customer") {
            CustomerFormScreen(navController = navController, agentId = agentId)
        }
        composable("edit_customer/{customerId}") { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")
            CustomerFormScreen(navController, agentId, customerId ?: "", isEdit = true)
        }
        composable("trial_expired") {
//            TrialExpiredScreen(navController)
        }
        composable("support") {
//            SupportScreen(navController)
        }
        composable("notifications") {
//            NotificationScreen(navController)
        }

        // Admin
        composable("admin_home") {
//            AdminHomeScreen(navController)
        }
    }
}
