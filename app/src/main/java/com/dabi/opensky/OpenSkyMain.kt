package com.dabi.opensky

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.dabi.opensky.core.designsystem.theme.OpenSkyTheme
import com.dabi.opensky.core.navigation.AppComposeNavigator
import com.dabi.opensky.core.navigation.OpenSkyScreen
import com.dabi.opensky.feature.session.SessionViewModel
import com.dabi.opensky.navigation.OpenSkyNavHost

@Composable
fun OpenSkyMain(
    composeNavigator: AppComposeNavigator<OpenSkyScreen>,
    sessionViewModel: SessionViewModel = hiltViewModel()

) {
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    OpenSkyTheme(
//        darkTheme = isDarkTheme  // ← Sử dụng user preference
    ) {
        val navHostController = rememberNavController()

        LaunchedEffect(Unit) {
            composeNavigator.handleNavigationCommands(navHostController)
        }
        // Luôn hiển thị NavHost để có background cho modal
        OpenSkyNavHost(navController = navHostController, sessionViewModel)

    }
}