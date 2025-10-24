package com.dabi.opensky.core.navigation

import kotlinx.serialization.Serializable

//Thêm các màn hình ở đây

sealed interface OpenSkyScreen {
    @Serializable
    data object Splash : OpenSkyScreen

    @Serializable
    data object Login : OpenSkyScreen

    // Main App Screens với Bottom Navigation
    @Serializable
    data object Home : OpenSkyScreen

    @Serializable
    data object Search : OpenSkyScreen

    @Serializable
    data object Favorites : OpenSkyScreen

    // ✅ NEW: Schedule detail route
    @Serializable
    data class ScheduleDetail(val scheduleId: String) : OpenSkyScreen

    @Serializable
    data object Profile : OpenSkyScreen

    @Serializable
    data object MyHotels : OpenSkyScreen

    @Serializable
    data class MyHotelEdit(val hotelId: String) : OpenSkyScreen // Sửa KS

    // core/navigation/OpenSkyScreen.kt
    @Serializable data object MyAssignments : OpenSkyScreen


    @Serializable data class ScheduleItinerary(val scheduleId: String, val tourId: String, val title: String) : OpenSkyScreen


    @Serializable
    data object Settings : OpenSkyScreen

    @Serializable data class TourDetail(val tourId: String) : OpenSkyScreen

    @Serializable
    data object EditProfile : OpenSkyScreen

    @Serializable
    data object BookingDetail : OpenSkyScreen

    @Serializable
    object ExploreHotel : OpenSkyScreen

    @Serializable
    object ExploreTour : OpenSkyScreen     // NEW

    @Serializable
    object ExploreActivity : OpenSkyScreen // NEW

    @Serializable data class MyBookingDetail(val bookingId: String) : OpenSkyScreen

    @Serializable
    data class TourBookingDetail(val bookingId: String)
    // Detail Screens
    @Serializable
    data class HotelDetail(val hotelId: String, val price: Double) : OpenSkyScreen

    @Serializable
    data class RoomScreen(val hotelId: String) : OpenSkyScreen

    @Serializable
    data class RoomDetail(val roomId: String) : OpenSkyScreen

    @Serializable
    data object MyBookings : OpenSkyScreen

    @Serializable
    data object MyVouchers : OpenSkyScreen

    // Legacy screens (có thể xóa sau)
    @Serializable
    data object LibraryScreen : OpenSkyScreen

    @Serializable
    data class LibraryDetailScreen(
        val albumName: String
    ) : OpenSkyScreen
    
    @Serializable
    data object LibraryDemo : OpenSkyScreen

    @Serializable
    data class Loading(
        val destination: String,
        val actionType: String = "" // Để xác định loại action cần thực thi
    ) : OpenSkyScreen

    //VD về màn hình có truyền data
//    @Serializable
//    data class Details(val pokemon: Pokemon) : PokedexScreen {
//        companion object {
//            val typeMap = mapOf(typeOf<Pokemon>() to PokemonType)
//        }
//    }
}