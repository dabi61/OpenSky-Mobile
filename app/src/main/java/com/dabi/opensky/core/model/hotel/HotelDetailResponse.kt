package com.dabi.opensky.core.model.hotel

import com.dabi.opensky.core.data.remote.model.response.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HotelDetailResponse(
    @Json(name = "hotelID")
    val hotelID: String,
    @Json(name = "hotelName")
    val hotelName: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "address")
    val address: String,
    @Json(name = "province")
    val province: String,
    @Json(name = "latitude")
    val latitude: Int,
    @Json(name = "longitude")
    val longitude: Int,
    @Json(name = "star")
    val star: Int,
    @Json(name = "status")
    val status: String,
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "hotelEmail")
    val hotelEmail: String,
    @Json(name = "images")
    val images: List<ImageHotel>,
    @Json(name = "user")
    val user: User,
)


@JsonClass(generateAdapter = true)
data class ImageHotel(
    @Json(name = "imageId")
    val imageId: String,
    @Json(name = "imageUrl")
    val imageUrl: String,
)