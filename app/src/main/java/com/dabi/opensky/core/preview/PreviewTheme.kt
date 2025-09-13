package com.dabi.opensky.core.preview

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.dabi.opensky.core.designsystem.theme.OpenSkyTheme
import com.dabi.opensky.core.navigation.LocalComposeNavigator
import com.dabi.opensky.core.navigation.OpenSkyComposeNavigator

@SuppressLint("UnusedSharedTransitionModifierParameter")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TrueCleanPreviewTheme(
    content: @Composable SharedTransitionScope.(AnimatedVisibilityScope) -> Unit,
) {
    CompositionLocalProvider(
        LocalComposeNavigator provides OpenSkyComposeNavigator(),
    ) {
        OpenSkyTheme {
            SharedTransitionScope {
                AnimatedVisibility(visible = true, label = "") {
                    content(this)
                }
            }
        }
    }
}
