package com.dabi.opensky.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dabi.opensky.core.designsystem.component.OpenSkyBottomNavigation
import com.dabi.opensky.core.navigation.OpenSkyScreen
import com.dabi.opensky.feature.session.SessionViewModel
import com.dabi.opensky.feature.session.TokenExpiredDialog

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun OpenSkyNavHost(
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val sessionUiState by sessionViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Always start with splash screen
    val startDestination = OpenSkyScreen.Splash
    
    // Handle logout navigation
    LaunchedEffect(sessionUiState.shouldNavigateToLogin) {
        if (sessionUiState.shouldNavigateToLogin) {
            navController.navigate(OpenSkyScreen.Login) {
                popUpTo(0) { inclusive = true }
            }
            sessionViewModel.onNavigatedToLogin()
        }
    }
    
    // Determine current screen for bottom nav
    val currentScreen = when {
        currentRoute?.contains("Home") == true -> OpenSkyScreen.Home
        currentRoute?.contains("Search") == true -> OpenSkyScreen.Search
        currentRoute?.contains("Favorites") == true -> OpenSkyScreen.Favorites
        currentRoute?.contains("Profile") == true -> OpenSkyScreen.Profile
        currentRoute?.contains("Settings") == true -> OpenSkyScreen.Settings
        else -> null
    }
    
    // Check if current screen should show bottom navigation
    val showBottomNav = when {
        currentRoute?.contains("Splash") == true -> false
        currentRoute?.contains("Login") == true -> false
        currentRoute?.contains("HotelDetail") == true -> false
        currentScreen != null -> true
        else -> false
    }
    
    // Default: All screens have status bar spacing unless they opt-out
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
//            .windowInsetsPadding(WindowInsets.statusBars), // Default status bar spacing
        bottomBar = {
            if (showBottomNav) {
                OpenSkyBottomNavigation(
                    currentRoute = currentScreen,
                    onNavigate = { screen ->
                        navController.navigate(screen) {
                            popUpTo(OpenSkyScreen.Home) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        contentWindowInsets = WindowInsets(0) // Don't add extra insets on top of status bar
    ) { paddingValues ->
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues)
            ) {
                openSkyNavigation(navController)
            }
        }
    }
    
    // Show token expired dialog
    if (sessionUiState.showTokenExpiredDialog) {
        TokenExpiredDialog(
            onDismiss = {
                sessionViewModel.dismissTokenExpiredDialog()
            },
            onConfirm = {
                sessionViewModel.logout()
                navController.navigate(OpenSkyScreen.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}