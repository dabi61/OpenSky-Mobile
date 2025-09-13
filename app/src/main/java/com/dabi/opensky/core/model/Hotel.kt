package com.dabi.opensky.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Hotel(
    @Json(name = "hotelID")
    val hotelID: String,
    @Json(name = "hotelName")
    val hotelName: String,
    @Json(name = "address")
    val address: String,
    @Json(name = "province")
    val province: String,
    @Json(name = "latitude")
    val latitude: String,
    @Json(name = "longitude")
    val longitude: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "star")
    val star: Int,
    @Json(name = "status")
    val status: String,
    @Json(name = "createdAt")
    val createdAt: String?,
    @Json(name = "images")
    val images: List<String>,
    @Json(name = "minPrice")
    val minPrice: Double,
    @Json(name = "maxPrice")
    val maxPrice: Double,
    @Json(name = "totalRooms")
    val totalRooms: Int,
    @Json(name = "availableRooms")
    val availableRooms: Int,
)