package com.dabi.opensky.core.navigation

import kotlinx.serialization.Serializable

//Thêm các màn hình ở đây

sealed interface OpenSkyScreen {
    @Serializable
    data object Home : OpenSkyScreen

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