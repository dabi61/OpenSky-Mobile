package com.dabi.opensky.core.data.remote.net

import com.dabi.opensky.core.model.room.Room

data class RoomNet(
    val roomID: String,
    val hotelID: String,
    val hotelName: String,
    val roomName: String,
    val roomType: String? = null,
    val address: String? = null,
    val price: Double,
    val maxPeople: Int? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val firstImage: String? = null
)

fun RoomNet.toDomain(): Room = Room(
    roomID = roomID,
    hotelID = hotelID,
    hotelName = hotelName,
    roomName = roomName,
    roomType = roomType,
    address = address,
    price = price,
    maxPeople = maxPeople,
    status = status,
    createdAt = createdAt,
    firstImage = firstImage,
    images = listOfNotNull(firstImage)
)