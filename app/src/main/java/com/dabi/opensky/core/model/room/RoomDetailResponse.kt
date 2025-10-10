// core/model/room/RoomDetailResponse.kt
package com.dabi.opensky.core.model.room

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RoomDetailResponse(
    @Json(name = "roomID") val roomID: String,
    @Json(name = "hotelID") val hotelID: String,
    @Json(name = "hotelName") val hotelName: String,
    @Json(name = "roomName") val roomName: String,
    @Json(name = "roomType") val roomType: String,
    @Json(name = "address") val address: String,
    @Json(name = "price") val price: Long,
    @Json(name = "maxPeople") val maxPeople: Int,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String,
    @Json(name = "images") val images: List<RoomImage>
)

@JsonClass(generateAdapter = true)
data class RoomImage(
    @Json(name = "imageId") val imageId: Long,
    @Json(name = "imageUrl") val imageUrl: String
)
