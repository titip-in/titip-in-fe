package com.titipin.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.ui.theme.*

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsState()

    LaunchedEffect(destination) {
        when (destination) {
            SplashDestination.Home        -> onNavigateToHome()
            SplashDestination.Login       -> onNavigateToLogin()
            SplashDestination.Onboarding  -> onNavigateToOnboarding()
            SplashDestination.Loading     -> Unit
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Charcoal),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Titip.in",
                fontSize = 42.sp, fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic, color = Cream, fontFamily = FrauncesFamily
            )
            Text(
                text = "Jastip & Preloved Malang",
                fontSize = 13.sp, color = Cream.copy(alpha = 0.5f), fontFamily = DmSansFamily
            )
        }
        Text(
            text = "v1.0.0", fontSize = 10.sp,
            color = Cream.copy(alpha = 0.2f), fontFamily = DmSansFamily,
            modifier = Modifier.align(Alignment.BottomCenter)
                .navigationBarsPadding().padding(bottom = Spacing.lg)
        )
    }
}