package com.dabi.opensky.core.data.remote.model

import com.squareup.moshi.Json

data class Room(
    @Json(name = "roomID")
    val roomID: String,
    @Json(name = "hotelID")
    val hotelID: String,
    @Json(name = "roomName")
    val roomName: String,
    @Json(name = "roomType")
    val roomType: String,
    @Json(name = "address")
    val address: String,
    @Json(name = "price")
    val price: Double,
    @Json(name = "maxPeople")
    val maxPeople: Int,
    @Json(name = "status")
    val status: String,
    @Json(name = "createAt")
    val createAt: String,
    @Json(name = "firstImage")
    val firstImage: String,
)