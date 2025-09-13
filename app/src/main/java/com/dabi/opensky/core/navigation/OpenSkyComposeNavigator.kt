package com.dabi.opensky.core.navigation


import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import javax.inject.Inject

class OpenSkyComposeNavigator @Inject constructor() : AppComposeNavigator<OpenSkyScreen>() {
    override fun navigate(
        route: OpenSkyScreen,
        optionsBuilder: (NavOptionsBuilder.() -> Unit)?
    ) {
        val options = optionsBuilder?.let { navOptions(it) }
        navigationCommands.tryEmit(ComposeNavigationCommand.NavigateToRoute(route, options))
    }

    override fun <R> navigateBackWithResult(
        key: String,
        result: R,
        route: OpenSkyScreen?
    ) {
        navigationCommands.tryEmit(
            ComposeNavigationCommand.NavigateUpWithResult(
                key = key,
                result = result,
                route = route,
            ),
        )
    }

    override fun popUpTo(route: OpenSkyScreen, inclusive: Boolean) {
        navigationCommands.tryEmit(ComposeNavigationCommand.PopUpToRoute(route, inclusive))
    }

    override fun navigateAndClearBackStack(route: OpenSkyScreen) {
        navigationCommands.tryEmit(
            ComposeNavigationCommand.NavigateToRoute(
                route,
                navOptions {
                    popUpTo(0)
                },
            ),
        )
    }
}