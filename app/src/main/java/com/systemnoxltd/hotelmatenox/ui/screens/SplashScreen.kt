package com.systemnoxltd.hotelmate.ui.screens

import android.util.Log
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
import com.systemnoxltd.hotelmate.utils.findActivity
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser

    val appUpdateManager = remember { AppUpdateManagerFactory.create(context) }

    var forceUpdate by remember { mutableStateOf(false) }
    var checkedUpdate by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset_anim"
    )

    LaunchedEffect(Unit) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { info ->
            val updateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
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
                } catch (e: Exception) {
                    Log.e("SplashScreen", "Update failed: ${e.localizedMessage}")
                }
            } else {
                checkedUpdate = true
            }
        }.addOnFailureListener {
            checkedUpdate = true
        }
    }

    if (!forceUpdate && checkedUpdate) {
        LaunchedEffect(Unit) {
            delay(2000)

            if (currentUser != null && currentUser.isEmailVerified) {
                // User is logged in and email is verified
                navController.navigate("agent_home") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                // User not logged in or not verified
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }

//            navController.navigate("login") {
//                popUpTo("splash") { inclusive = true }
//            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Logo",
            modifier = Modifier.offset(y = offsetY.dp)
        )
    }
}
