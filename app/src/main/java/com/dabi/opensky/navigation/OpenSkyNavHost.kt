package com.dabi.opensky.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dabi.opensky.core.navigation.OpenSkyScreen
import com.dabi.opensky.feature.session.SessionViewModel
import com.dabi.opensky.feature.session.TokenExpiredDialog
import com.dabi.opensky.core.navigation.AppComposeNavigator
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.dabi.opensky.core.navigation.LocalComposeNavigator

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun OpenSkyNavHost(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val sessionUiState by sessionViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Always start with splash screen
    val startDestination = OpenSkyScreen.Splash
    var selected by remember { mutableStateOf(0) }
    // Handle logout navigation
    LaunchedEffect(sessionUiState.shouldNavigateToLogin) {
        if (sessionUiState.shouldNavigateToLogin) {
            Log.d("OpenSkyNavHost", "Navigating to login")
            navController.navigate(OpenSkyScreen.Login) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
                restoreState = false
            }
            sessionViewModel.onNavigatedToLogin()
        }
    }

    // Determine current screen for bottom nav
//    val currentScreen = when {
//        currentRoute?.contains("Home") == true -> OpenSkyScreen.Home
//        currentRoute?.contains("Search") == true -> OpenSkyScreen.Search
//        currentRoute?.contains("Favorites") == true -> OpenSkyScreen.Favorites
//        currentRoute?.contains("Profile") == true -> OpenSkyScreen.Profile
//        currentRoute?.contains("Settings") == true -> OpenSkyScreen.Settings
//        else -> null
//    }
//
//    // Check if current screen should show bottom navigation
//    val showBottomNav = when {
//        currentRoute?.contains("Splash") == true -> false
//        currentRoute?.contains("Login") == true -> false
//        currentRoute?.contains("HotelDetail") == true -> false
//        currentScreen != null -> true
//        else -> false
//    }

    // ✅ Tạo navigator một lần từ navController hiện tại

//    / ✅ BƠM LocalComposeNavigator CHO TOÀN BỘ CÂY
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            openSkyNavigation(navController, sessionViewModel)
        }
    }
    // Token expired dialog vẫn nằm trong provider để có thể navigate nếu cần
    if (sessionUiState.showTokenExpiredDialog) {
        TokenExpiredDialog(
            onDismiss = { sessionViewModel.dismissTokenExpiredDialog() },
            onConfirm = {
                sessionViewModel.logout()
                navController.navigate(OpenSkyScreen.Login) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                    restoreState = false
                }
            }
        )
    }
}