package com.systemnoxltd.hotelmatenox.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.systemnoxltd.hotelmatenox.ui.auth.LoginScreen
import com.systemnoxltd.hotelmatenox.ui.auth.SignUpScreen
import com.systemnoxltd.hotelmatenox.ui.screens.SplashScreen
import com.systemnoxltd.hotelmatenox.ui.auth.EmailVerificationScreen
import com.systemnoxltd.hotelmatenox.ui.auth.ForgotPasswordScreen
import com.systemnoxltd.hotelmatenox.ui.screens.AddOrEditClientScreen
import com.systemnoxltd.hotelmatenox.ui.screens.AddOrEditHotelScreen
import com.systemnoxltd.hotelmatenox.ui.screens.AgentHomeScreen
import com.systemnoxltd.hotelmatenox.ui.screens.ClientsScreen
import com.systemnoxltd.hotelmatenox.ui.screens.CustomerFormScreen
import com.systemnoxltd.hotelmatenox.ui.screens.HotelsScreen
import com.systemnoxltd.hotelmatenox.ui.screens.PaymentsScreen
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
        composable("splash",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) { SplashScreen(navController) }
        composable("login",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) { LoginScreen(navController) }
        composable("forgot_password",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) { ForgotPasswordScreen(navController) }
        composable("signup",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) { SignUpScreen(navController) }
        composable("email_verification",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) { EmailVerificationScreen(navController) }

        // Main tabs
        composable(BottomNavItem.Home.route,
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
            AgentHomeScreen(navController, agentId = agentId, navigateWithInterstitial = navigateWithInterstitial)
        }
        composable(BottomNavItem.Clients.route,
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
            ClientsScreen(navController, navigateWithInterstitial = navigateWithInterstitial)
        }
        composable(BottomNavItem.Hotels.route,
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
            HotelsScreen(navController, navigateWithInterstitial = navigateWithInterstitial)
        }

        // Extra screens
        composable("add_customer",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
            CustomerFormScreen(navController, agentId)
        }
        composable("edit_customer/{customerId}",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
            val id = it.arguments?.getString("customerId") ?: ""
            CustomerFormScreen(navController, agentId, id, isEdit = true)
        }
        composable("add_client",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
            AddOrEditClientScreen(navController, isEdit = false)
        }
        composable("edit_client/{id}",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
            val id = it.arguments?.getString("id") ?: ""
            AddOrEditClientScreen(navController, isEdit = true, clientId = id)
        }
        composable("add_hotel",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
            AddOrEditHotelScreen(navController, isEdit = false)
        }
        composable("edit_hotel/{id}",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
            val id = it.arguments?.getString("id") ?: ""
            AddOrEditHotelScreen(navController, isEdit = true, hotelId = id)
        }
        composable("payments_screen/{clientId}",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
            val clientId = it.arguments?.getString("clientId") ?: ""
            PaymentsScreen(navController, agentId = agentId, clientId = clientId)
        }
        composable("profile",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
            ProfileScreen(navController)
        }

        // Optional placeholders
        composable("trial_expired") { /* TrialExpiredScreen(navController) */ }
        composable("support") { /* SupportScreen(navController) */ }
        composable("notifications") { /* NotificationScreen(navController) */ }
        composable("help",enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
//            HelpScreen(navController)
        }
        // Admin
        composable("admin_home",
            enterTransition = { slideInHorizontally(tween(300)) { fullWidth -> fullWidth } },
            exitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popEnterTransition = { slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } },
            popExitTransition = { slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } }) {
//            AdminHomeScreen(navController)
        }
    }
}
