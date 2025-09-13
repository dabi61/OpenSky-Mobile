package com.dabi.opensky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.dabi.opensky.core.designsystem.theme.OpenSkyTheme
import com.dabi.opensky.navigation.OpenSkyNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenSkyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OpenSkyNavHost(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}