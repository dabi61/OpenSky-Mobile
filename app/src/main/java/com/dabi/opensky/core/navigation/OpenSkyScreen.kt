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
    
    @Serializable
    data object Profile : OpenSkyScreen
    
    @Serializable
    data object Settings : OpenSkyScreen
    
    // Detail Screens
    @Serializable
    data class HotelDetail(val hotelId: String) : OpenSkyScreen

    @Serializable
    data class RoomScreen(val hotelId: String) : OpenSkyScreen

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