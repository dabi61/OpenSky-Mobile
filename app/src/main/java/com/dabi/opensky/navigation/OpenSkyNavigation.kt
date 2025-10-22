package com.dabi.opensky.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dabi.opensky.core.navigation.OpenSkyScreen
import com.dabi.opensky.feature.billing.MyBillsScreen
import com.dabi.opensky.feature.booking.BookingDetailScreen
import com.dabi.opensky.feature.booking.MyBookingsScreen
import com.dabi.opensky.feature.favorites.FavoritesScreen
import com.dabi.opensky.feature.home.HomeScreen
import com.dabi.opensky.feature.hotel.HotelDetailScreen
import com.dabi.opensky.feature.hotel.HotelsExploreScreen
import com.dabi.opensky.feature.login.LoginScreen
import com.dabi.opensky.feature.profile.EditProfileScreen
import com.dabi.opensky.feature.profile.ProfileScreen
import com.dabi.opensky.feature.rooms.HotelRoomsScreen
import com.dabi.opensky.feature.search.SearchScreen
import com.dabi.opensky.feature.session.SessionViewModel
import com.dabi.opensky.feature.settings.SettingsScreen
import com.dabi.opensky.feature.splash.SplashScreen
import com.dabi.opensky.feature.tour.TourBookingDetailScreen


@RequiresApi(Build.VERSION_CODES.O)
context(SharedTransitionScope)
fun NavGraphBuilder.openSkyNavigation(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
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
            },
            sessionViewModel = sessionViewModel,
            onExploreHotels = {
                navController.navigate(OpenSkyScreen.ExploreHotel)
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
        ProfileScreen(
            onEditDetail = {
                navController.navigate(OpenSkyScreen.EditProfile)
            },
            onBookingDetail = {
                navController.navigate(OpenSkyScreen.BookingDetail)
            },
            sessionViewModel = sessionViewModel
        )
    }

    composable<OpenSkyScreen.BookingDetail>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        MyBillsScreen(
            onBack = {
                navController.popBackStack()
            }
        )
    }

    // Explore Hotel (đang có) — bổ sung onHotelClick để đi tới chi tiết
    composable<OpenSkyScreen.ExploreHotel>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition  = { fadeOut(tween(300)) }
    ) {
        HotelsExploreScreen(
//            onBack = { navController.popBackStack() },
            onHotelClick = { id -> navController.navigate(OpenSkyScreen.HotelDetail(id)) } // NEW
        )
    }
//
//// Explore Tour – placeholder để bạn triển khai sau
//    composable<OpenSkyScreen.ExploreTour>(
//        enterTransition = { fadeIn(tween(300)) },
//        exitTransition  = { fadeOut(tween(300)) }
//    ) {
//        ToursScreen( // hoặc tên bạn đặt
//            onBack = { navController.popBackStack() }
//        )
//    }
//
//// Explore Activity – placeholder để bạn triển khai sau
//    composable<OpenSkyScreen.ExploreActivity>(
//        enterTransition = { fadeIn(tween(300)) },
//        exitTransition  = { fadeOut(tween(300)) }
//    ) {
//        ActivitiesScreen(
//            onBack = { navController.popBackStack() }
//        )
//    }

    composable<OpenSkyScreen.MyBookings> {
        MyBookingsScreen(
            onBack = { navController.popBackStack() },
            onOpenBookingDetail = { id -> navController.navigate(OpenSkyScreen.MyBookingDetail(id)) },
            onOpenTourBookingDetail = { id -> navController.navigate(OpenSkyScreen.TourBookingDetail(id)) }
        )
    }

    composable<OpenSkyScreen.MyBookingDetail> {
        val args = it.toRoute<OpenSkyScreen.MyBookingDetail>()
        BookingDetailScreen(
            bookingId = args.bookingId,
            onBack = { navController.popBackStack() }
        )
    }

    // navigation/OpenSkyNavigation.kt
    composable<OpenSkyScreen.TourBookingDetail>(
        enterTransition = { fadeIn() }, exitTransition = { fadeOut() }
    ) { backStack ->
        val route = backStack.toRoute<OpenSkyScreen.TourBookingDetail>()
        TourBookingDetailScreen(
            bookingId = route.bookingId,
            onBack = { navController.popBackStack() }
        )
    }

    composable<OpenSkyScreen.EditProfile>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) { backStackEntry ->
        EditProfileScreen(
            onBack = { navController.popBackStack() },
        )
    }
    
    composable<OpenSkyScreen.Settings>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        SettingsScreen()
    }
    
    composable<OpenSkyScreen.HotelDetail>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) { backStackEntry ->
        val hotelDetail = backStackEntry.toRoute<OpenSkyScreen.HotelDetail>()
//        Log.d("HotelDetailScreen", "Hotel ID: ${hotelDetail.hotelId}")
        HotelDetailScreen(
            hotelId = hotelDetail.hotelId,
            onBackClick = { navController.popBackStack() },
            onSeeRooms = {
                navController.navigate(OpenSkyScreen.RoomScreen(hotelId = hotelDetail.hotelId))
            }
        )
    }

    composable<OpenSkyScreen.RoomScreen> {
        val args = it.toRoute<OpenSkyScreen.RoomScreen>()
        HotelRoomsScreen(
            hotelId = args.hotelId,
            onBack = { navController.popBackStack() },
            onRoomClick = { room ->
                navController.navigate(OpenSkyScreen.RoomDetail(roomId = room.id)) // 👈 sang chi tiết phòng
            }
        )
    }

    // 👇 Thêm mới: màn chi tiết phòng + đặt phòng
    composable<OpenSkyScreen.RoomDetail>(
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) { backStackEntry ->
        val args = backStackEntry.toRoute<OpenSkyScreen.RoomDetail>()
        com.dabi.opensky.feature.roomdetail.RoomDetailScreen(
            roomId = args.roomId,
            onBack = { navController.popBackStack() }
        )
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
