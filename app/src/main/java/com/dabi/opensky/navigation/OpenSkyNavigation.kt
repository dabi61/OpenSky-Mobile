package com.dabi.opensky.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dabi.opensky.core.navigation.OpenSkyScreen
import com.dabi.opensky.feature.favorites.FavoritesScreen
import com.dabi.opensky.feature.home.HomeScreen
import com.dabi.opensky.feature.login.LoginScreen
import com.dabi.opensky.feature.profile.ProfileScreen
import com.dabi.opensky.feature.search.SearchScreen
import com.dabi.opensky.feature.settings.SettingsScreen
import com.dabi.opensky.feature.splash.SplashScreen


context(SharedTransitionScope)
fun NavGraphBuilder.openSkyNavigation(
    navController: NavHostController
) {

    composable<OpenSkyScreen.Splash> {
        SplashScreen(
            onNavigateToLogin = {
                navController.navigate(OpenSkyScreen.Login) {
                    popUpTo(OpenSkyScreen.Splash) { inclusive = true }
                }
            },
            onNavigateToHome = {
                navController.navigate(OpenSkyScreen.Home) {
                    popUpTo(OpenSkyScreen.Splash) { inclusive = true }
                }
            }
        )
    }
    
    composable<OpenSkyScreen.Login> {
        LoginScreen(
            onLoginSuccess = {
                navController.navigate(OpenSkyScreen.Home) {
                    popUpTo(OpenSkyScreen.Login) { inclusive = true }
                }
            }
        )
    }
    
    composable<OpenSkyScreen.Home>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        HomeScreen(
            onHotelClick = { hotelId ->
                navController.navigate(OpenSkyScreen.HotelDetail(hotelId))
            }
        )
    }
    
    composable<OpenSkyScreen.Search>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        SearchScreen(
            onHotelClick = { hotelId ->
                navController.navigate(OpenSkyScreen.HotelDetail(hotelId))
            }
        )
    }
    
    composable<OpenSkyScreen.Favorites>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        FavoritesScreen(
            onHotelClick = { hotelId ->
                navController.navigate(OpenSkyScreen.HotelDetail(hotelId))
            }
        )
    }
    
    composable<OpenSkyScreen.Profile>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        ProfileScreen()
    }
    
    composable<OpenSkyScreen.Settings>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        SettingsScreen()
    }
    
    composable<OpenSkyScreen.HotelDetail> { backStackEntry ->
        val hotelDetail = backStackEntry.toRoute<OpenSkyScreen.HotelDetail>()
        // TODO: Create HotelDetailScreen
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hotel Detail",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Hotel ID: ${hotelDetail.hotelId}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Quay lại")
                }
            }
        }
    }
}
//
//    // Màn Library (push từ Home)
//    composable<TrueCleanScreen.LibraryScreen>(
////        enterTransition = { iosPushEnter() },
////        exitTransition = { iosPushExit() },
////        popEnterTransition = { iosPopEnter() },
////        popExitTransition = { iosPopExit() }
//    ) { backStackEntry ->
//        ProductionLibraryScreen(
//            animatedVisibilityScope = this,
//            // 🎯 WORKING SOLUTION: NavController-scoped ViewModel
//        )
//    }
//
//    // Màn Library Detail (push từ LibraryScreen)
//    composable<TrueCleanScreen.LibraryDetailScreen>(
////        enterTransition = { iosPushEnter() },
////        exitTransition = { iosPushExit() },
////        popEnterTransition = { iosPopEnter() },
////        popExitTransition = { iosPopExit() }
//    ) { backStackEntry ->
//        ProductionLibraryDetailScreen(
//            animatedVisibilityScope = this,
//            // 🎯 SIMPLE SOLUTION: Get ViewModel from LibraryScreen parent
//        )
//    }
//
//
//    // Demo màn hình để thấy kết quả loading
//    composable<TrueCleanScreen.LibraryDemo>(
////        enterTransition = { iosPushEnter() },
////        exitTransition = { iosPushExit() },
////        popEnterTransition = { iosPopEnter() },
////        popExitTransition = { iosPopExit() }
//    ) {
//        LibraryLoadingDemo(this)
//    }
//
//    composable<TrueCleanScreen.Loading>(
////        enterTransition = { iosPushEnter() },
////        exitTransition = { iosPushExit() },
////        popEnterTransition = { iosPopEnter() },
////        popExitTransition = { iosPopExit() }
//    ) { backStackEntry ->
//        val loadingScreen = backStackEntry.toRoute<TrueCleanScreen.Loading>()
//
//        // Sử dụng LibraryIntegratedLoading cho Library, TrueCleanerLoading cho các màn khác
//        when (loadingScreen.destination) {
//            "Library" -> {
//                LibraryIntegratedLoading(
//                    animatedVisibilityScope = this
//                )
//            }
//            else -> {
//                TrueCleanerLoading(
//                    animatedVisibilityScope = this,
//                    destination = loadingScreen.destination,
//                    actionType = loadingScreen.actionType
//                )
//            }
//        }
//    }

//    composable<PokedexScreen.Details>(
//        typeMap = PokedexScreen.Details.typeMap,
//        enterTransition = { slideInFromRight() },
//        exitTransition = { slideOutToRight() },
//        popEnterTransition = { slideInFromLeft() },
//        popExitTransition = { slideOutToLeft() }
//    ) {
//        PokedexDetails(this)
//    }
//}
