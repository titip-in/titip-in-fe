package com.titipin.app.ui.navigation


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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.titipin.app.navigation.Routes
import com.titipin.app.ui.auth.LoginScreen
import com.titipin.app.ui.auth.RegisterScreen
import com.titipin.app.ui.home.HomeScreen
import com.titipin.app.ui.jastip.JastipDetailScreen
import com.titipin.app.ui.jastip.JastipOfferScreen
import com.titipin.app.ui.jastip.JastipRequestDetailScreen
import com.titipin.app.ui.jastip.JastipScreen
import com.titipin.app.ui.onboarding.OnboardingScreen
import com.titipin.app.ui.onboarding.SetupProfileScreen
import com.titipin.app.ui.preloved.PrelovedDetailScreen
import com.titipin.app.ui.preloved.PrelovedRequestDetailScreen
import com.titipin.app.ui.preloved.PrelovedScreen
import com.titipin.app.ui.profile.JastipSayaScreen
import com.titipin.app.ui.profile.PengaturanScreen
import com.titipin.app.ui.profile.PrelovedSayaScreen
import com.titipin.app.ui.profile.ProfileScreen
import com.titipin.app.ui.profile.AnalyticsScreen
import com.titipin.app.ui.profile.ReviewRatingScreen
import com.titipin.app.ui.splash.SplashScreen
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
    Routes.SPLASH,
    Routes.ONBOARDING,
    Routes.SETUP_PROFILE,
    Routes.LOGIN,
    Routes.REGISTER,
    Routes.JASTIP_DETAIL_PATTERN,
    Routes.JASTIP_REQUEST_DETAIL_PATTERN,
    Routes.PRELOVED_DETAIL_PATTERN,
    Routes.PRELOVED_REQUEST_DETAIL_PATTERN,
    Routes.JASTIP_SAYA,
    Routes.PRELOVED_SAYA,
    Routes.REVIEW_RATING,
    Routes.PENGATURAN,
    Routes.ANALYTICS,
    Routes.JASTIP_OFFER_PATTERN,
)

@Composable
fun TitipinNavGraph(
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val sessionMessage by sessionViewModel.sessionMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val showBottomNav = navBackStackEntry?.destination?.hierarchy?.none { dest ->
        dest.route in routesWithoutBottomNav
    } ?: false
    var bottomNavVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute, showBottomNav) {
        if (showBottomNav) {
            kotlinx.coroutines.delay(320)
            bottomNavVisible = true
        } else {
            bottomNavVisible = false
        }
    }

    LaunchedEffect(sessionMessage) {
        val message = sessionMessage ?: return@LaunchedEffect
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
        snackbarHostState.showSnackbar(message)
        sessionViewModel.consumeSessionMessage()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController    = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier
                .fillMaxSize()
                .then(if (showBottomNav) Modifier.padding(bottom = 90.dp) else Modifier),
            enterTransition     = { fadeIn(animationSpec = tween(300)) },
            exitTransition      = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition  = { fadeIn(animationSpec = tween(300)) },
            popExitTransition   = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToSetupProfile = {
                        navController.navigate(Routes.SETUP_PROFILE) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onSetupRequired = {
                        navController.navigate(Routes.SETUP_PROFILE) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.SETUP_PROFILE) {
                SetupProfileScreen(
                    onFinish = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SETUP_PROFILE) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToJastip      = { navController.navigate(Routes.JASTIP) },
                    onNavigateToPreloved    = { navController.navigate(Routes.PRELOVED) },
                    onNavigateToJastipDetail = { id -> navController.navigate(Routes.jastipDetail(id)) },
                    onNavigateToJastipRequestDetail = { id -> navController.navigate(Routes.jastipRequestDetail(id)) },
                    onNavigateToPrelovedDetail = { id -> navController.navigate(Routes.prelovedDetail(id)) },
                    onNavigateToPrelovedRequestDetail = { id -> navController.navigate(Routes.prelovedRequestDetail(id)) },
                    onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                    onNavigateToPengaturan  = { navController.navigate(Routes.PENGATURAN) }
                )
            }
            composable(Routes.PRELOVED) {
                PrelovedScreen(
                    onNavigateToDetail = { id ->
                        navController.navigate(Routes.prelovedDetail(id))
                    },
                    onNavigateToRequestDetail = { id ->
                        navController.navigate(Routes.prelovedRequestDetail(id))
                    }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToJastipSaya   = { navController.navigate(Routes.JASTIP_SAYA) },
                    onNavigateToPrelovedSaya = { navController.navigate(Routes.PRELOVED_SAYA) },
                    onNavigateToReview       = { navController.navigate(Routes.REVIEW_RATING) },
                    onNavigateToPengaturan   = { navController.navigate(Routes.PENGATURAN) },
                    onNavigateToAnalytics    = { navController.navigate(Routes.ANALYTICS) }
                )
            }

            composable(Routes.JASTIP_SAYA) {
                JastipSayaScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToDetail = { id ->
                        navController.navigate(Routes.jastipDetail(id))
                    },
                    onNavigateToRequestDetail = { id ->
                        navController.navigate(Routes.jastipRequestDetail(id))
                    }
                )
            }

            composable(Routes.PRELOVED_SAYA) {
                PrelovedSayaScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToDetail = { id ->
                        navController.navigate(Routes.prelovedDetail(id))
                    },
                    onNavigateToRequestDetail = { id ->
                        navController.navigate(Routes.prelovedRequestDetail(id))
                    }
                )
            }

            composable(Routes.REVIEW_RATING) {
                ReviewRatingScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.PENGATURAN) {
                PengaturanScreen(
                    onBack = { navController.popBackStack() },
                    onDeleteAccountSuccess = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.ANALYTICS) { backStackEntry ->
                // Pass tier dari parent back stack jika tersedia
                val tier = backStackEntry.arguments?.getString("tier") ?: ""
                AnalyticsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToUpgrade = { navController.navigate(Routes.PROFILE) }
                )
            }

            // JastipScreen sekarang handle bottom sheet sendiri di dalamnya
            composable(Routes.JASTIP) {
                JastipScreen(
                    onNavigateToDetail = { id ->
                        navController.navigate(Routes.jastipDetail(id))
                    },
                    onNavigateToRequestDetail = { id ->
                        navController.navigate(Routes.jastipRequestDetail(id))
                    },
                    onNavigateToOffer = { from, to, name, notes ->
                        navController.navigate(Routes.jastipOffer(from, to, name, notes))
                    }
                )
            }

            composable(
                route = Routes.JASTIP_OFFER_PATTERN,
                arguments = listOf(
                    androidx.navigation.navArgument("from")  { defaultValue = "" },
                    androidx.navigation.navArgument("to")    { defaultValue = "" },
                    androidx.navigation.navArgument("name")  { defaultValue = "" },
                    androidx.navigation.navArgument("notes") { defaultValue = "" },
                )
            ) { backStackEntry ->
                val from  = backStackEntry.arguments?.getString("from")  ?: ""
                val to    = backStackEntry.arguments?.getString("to")    ?: ""
                val name  = backStackEntry.arguments?.getString("name")  ?: ""
                val notes = backStackEntry.arguments?.getString("notes") ?: ""
                JastipOfferScreen(
                    fromLocation   = from,
                    toLocation     = to,
                    requesterName  = name,
                    notes          = notes.ifEmpty { null },
                    onBack         = { navController.popBackStack() }
                )
            }

            composable(Routes.JASTIP_DETAIL_PATTERN) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                JastipDetailScreen(
                    jastipId = id,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.JASTIP_REQUEST_DETAIL_PATTERN) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                JastipRequestDetailScreen(
                    requestId = id,
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

            composable(Routes.PRELOVED_REQUEST_DETAIL_PATTERN) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                PrelovedRequestDetailScreen(
                    requestId = id,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (bottomNavVisible) {
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
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                data,
                containerColor = Charcoal,
                contentColor = Cream,
                actionColor = Terracotta,
                shape = RoundedCornerShape(Radius.md)
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
