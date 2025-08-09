package com.systemnoxltd.hotelmatenox.ui.auth

import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import com.systemnoxltd.hotelmatenox.R

@Composable
fun EmailVerificationScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Optional: Refresh user data every 5 seconds to check verification
        while (!user?.isEmailVerified!!) {
            delay(5000)
            user.reload()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("A verification email has been sent to your email address.")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Please verify to continue using the app.")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true
                user?.sendEmailVerification()
                    ?.addOnSuccessListener {
                        Toast.makeText(context, "Verification email sent again.", Toast.LENGTH_SHORT).show()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(context, "Failed to send email: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    ?.addOnCompleteListener {
                        isLoading = false
                    }
            },
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Resend Email")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                user?.reload()?.addOnCompleteListener {
                    if (user.isEmailVerified) {
                        navController.navigate("agent_home") {
                            popUpTo("email_verification") { inclusive = true }
                        }
                    } else {
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, "Email not verified yet.", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo("email_verification") { inclusive = true }
                        }
                    }
                }
            }
        ) {
            Text("Continue")
        }
    }
}
