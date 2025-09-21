package com.dabi.opensky.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf


//Nguồn cung cấp navigator trong  mặc định ném lỗi nếu như không tìm thấy
val LocalComposeNavigator : ProvidableCompositionLocal<AppComposeNavigator<OpenSkyScreen>> =
    compositionLocalOf {
        error(
            "No AppComposeNavigator provided! " +
            "Make sure to wrap all usages of TrueClean Component in TrueCleanTheme"
        )
    }


/**
 * Retrieves the current [AppComposeNavigator] at the call site's position in the hierarchy.
 */
val currentComposeNavigator: AppComposeNavigator<OpenSkyScreen>
    @Composable
    @ReadOnlyComposable
    get() = LocalComposeNavigator.current


//Dùng trong UI val compose = currentComposeNavigator để gọi navigate/navigateUp mà không cần truyền prop