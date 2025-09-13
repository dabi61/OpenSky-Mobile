package com.dabi.opensky.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.dabi.opensky.core.navigation.OpenSkyScreen
import com.dabi.opensky.feature.main.MainScreen
import com.dabi.opensky.feature.session.SessionViewModel
import com.dabi.opensky.feature.session.TokenExpiredDialog

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun OpenSkyNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val sessionUiState by sessionViewModel.uiState.collectAsState()

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
    
        MainScreen(navController = navController) { paddingValues ->
            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = modifier.padding(paddingValues)
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