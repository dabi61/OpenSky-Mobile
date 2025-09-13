package com.dabi.opensky.feature.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dabi.opensky.core.designsystem.component.OpenSkyBottomNavigation
import com.dabi.opensky.core.navigation.OpenSkyScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Convert route string to OpenSkyScreen - Use contains for better matching
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

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                OpenSkyBottomNavigation(
                    currentRoute = currentScreen,
                    onNavigate = { screen ->
                        navController.navigate(screen) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(OpenSkyScreen.Home) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when reselecting
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}
