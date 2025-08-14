package com.systemnoxltd.hotelmatenox.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.systemnoxltd.hotelmatenox.R
import com.systemnoxltd.hotelmatenox.utils.findActivity
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser

    var forceUpdate by remember { mutableStateOf(false) }
    var checkedUpdate by remember { mutableStateOf(false) }

    // Animation: smooth up and down
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Launch update check separately
    LaunchedEffect(Unit) {
        val appUpdateManager = AppUpdateManagerFactory.create(context)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { info ->
            val updateAvailable =
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            if (updateAvailable) {
                forceUpdate = true
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        AppUpdateType.IMMEDIATE,
                        context.findActivity(),
                        100
                    )
                } catch (_: Exception) {}
            } else {
                checkedUpdate = true
            }
        }.addOnFailureListener { checkedUpdate = true }
    }

    // Navigate after delay if no update
    if (!forceUpdate && checkedUpdate) {
        LaunchedEffect(Unit) {
            delay(2000)
            if (currentUser != null && currentUser.isEmailVerified) {
                navController.navigate("agent_home") { popUpTo("splash") { inclusive = true } }
            } else {
                navController.navigate("login") { popUpTo("splash") { inclusive = true } }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_hotel_mate),
            contentDescription = "Logo",
            modifier = Modifier
                .offset(y = offsetY.dp)
                .size(120.dp) // ensure reasonable size for performance
        )
    }
}
