package com.dabi.opensky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowInsetsControllerCompat
import com.dabi.opensky.core.designsystem.theme.OpenSkyTheme
import com.dabi.opensky.core.navigation.AppComposeNavigator
import com.dabi.opensky.core.navigation.LocalComposeNavigator
import com.dabi.opensky.core.navigation.OpenSkyScreen
import com.dabi.opensky.navigation.OpenSkyNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    internal lateinit var composeNavigator: AppComposeNavigator<OpenSkyScreen>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge for immersive experience
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true   // false = icon sáng (trắng)
        }
        setContent {
            // Cấu hình global providers
            CompositionLocalProvider(
                LocalComposeNavigator provides composeNavigator,
//                LocalUserPreferences provides userPreferences,  // ← Provide user settings
//                LocalContext provides this@MainActivity,
            ) {
                OpenSkyMain(
                    composeNavigator = composeNavigator,
                )
//                ,userPreferences = userPreferences
            }
        }

    }
}