package com.titipin.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.titipin.app.ui.auth.LoginScreen
import com.titipin.app.ui.auth.RegisterScreen
import com.titipin.app.ui.home.HomeScreen
import com.titipin.app.ui.jastip.JastipDetailScreen
import com.titipin.app.ui.jastip.JastipScreen
import com.titipin.app.ui.preloved.PrelovedDetailScreen
import com.titipin.app.ui.preloved.PrelovedScreen
import com.titipin.app.ui.profile.ProfileScreen
import com.titipin.app.ui.theme.*

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavEmojis = mapOf(
    Routes.HOME     to "🏠",
    Routes.JASTIP   to "📦",
    Routes.PRELOVED to "🛍️",
    Routes.PROFILE  to "👤",
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME,     "Home",     Icons.Rounded.Home),
    BottomNavItem(Routes.JASTIP,   "Jastip",   Icons.Rounded.ShoppingBag),
    BottomNavItem(Routes.PRELOVED, "Preloved", Icons.Rounded.Storefront),
    BottomNavItem(Routes.PROFILE,  "Profil",   Icons.Rounded.Person),
)

// Form screens dihapus dari sini — sekarang pakai ModalBottomSheet
// Bottom nav disembunyikan hanya di auth screens dan detail screens
val routesWithoutBottomNav = listOf(
    Routes.LOGIN,
    Routes.REGISTER,
    Routes.JASTIP_DETAIL_PATTERN,
    Routes.PRELOVED_DETAIL_PATTERN,
)

@Composable
fun TitipinNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomNav = navBackStackEntry?.destination?.hierarchy?.none { dest ->
        dest.route in routesWithoutBottomNav
    } ?: false

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController    = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier
                .fillMaxSize()
                .then(if (showBottomNav) Modifier.padding(bottom = 90.dp) else Modifier),
            enterTransition     = { fadeIn(animationSpec = tween(300)) },
            exitTransition      = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition  = { fadeIn(animationSpec = tween(300)) },
            popExitTransition   = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME)     { HomeScreen() }
            composable(Routes.PRELOVED) {
                PrelovedScreen(
                    onNavigateToDetail = { id ->
                        navController.navigate(Routes.prelovedDetail(id))
                    }
                )
            }
            composable(Routes.PROFILE)  { ProfileScreen() }

            // JastipScreen sekarang handle bottom sheet sendiri di dalamnya
            composable(Routes.JASTIP) {
                JastipScreen(
                    onNavigateToDetail = { id ->
                        navController.navigate(Routes.jastipDetail(id))
                    }
                )
            }

            composable(Routes.JASTIP_DETAIL_PATTERN) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                JastipDetailScreen(
                    jastipId = id,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PRELOVED_DETAIL_PATTERN) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                PrelovedDetailScreen(
                    prelovedId = id,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (showBottomNav) {
            TitipinBottomNav(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
fun TitipinBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), ambientColor = Charcoal.copy(alpha = 0.15f), spotColor = Charcoal.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(24.dp)),
        containerColor = Charcoal,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected        = isSelected,
                alwaysShowLabel = true,
                onClick         = { onNavigate(item.route) },
                icon  = { Text(bottomNavEmojis[item.route] ?: "●", fontSize = if (isSelected) 20.sp else 18.sp) },
                label = { Text(item.label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp, fontFamily = DmSansFamily) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Cream,
                    selectedTextColor   = Cream,
                    unselectedIconColor = Charcoal30,
                    unselectedTextColor = InactiveText,
                    indicatorColor      = Terracotta
                )
            )
        }
    }
}