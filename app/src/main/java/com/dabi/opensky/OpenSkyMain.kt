package com.dabi.opensky

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.dabi.opensky.core.designsystem.theme.OpenSkyTheme
import com.dabi.opensky.core.navigation.AppComposeNavigator
import com.dabi.opensky.core.navigation.OpenSkyScreen
import com.dabi.opensky.navigation.OpenSkyNavHost

@Composable
fun OpenSkyMain(
    composeNavigator: AppComposeNavigator<OpenSkyScreen>,
) {
    OpenSkyTheme(
//        darkTheme = isDarkTheme  // ← Sử dụng user preference
    ) {
        val navHostController = rememberNavController()

        LaunchedEffect(Unit) {
            composeNavigator.handleNavigationCommands(navHostController)
        }

        // Luôn hiển thị NavHost để có background cho modal
        OpenSkyNavHost(navController = navHostController)

    }
}