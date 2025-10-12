package com.dabi.opensky.core.model.room

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class RoomsResponse(
    @Json(name = "rooms") val rooms: List<RoomItem>,
    @Json(name = "currentPage") val currentPage: Int,
    @Json(name = "pageSize") val pageSize: Int,
    @Json(name = "totalRooms") val totalRooms: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "hasNextPage") val hasNextPage: Boolean,
    @Json(name = "hasPreviousPage") val hasPreviousPage: Boolean
)

@JsonClass(generateAdapter = true)
data class RoomItem(
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
    @Json(name = "firstImage") val firstImage: String?
)

/* (Tùy chọn) Domain tối giản để UI dùng gọn */
data class Room(
    val id: String,
    val name: String,
    val type: String,
    val price: Long,
    val capacity: Int,
    val status: String,
    val imageUrl: String?,
    val address: String
)

fun RoomItem.toDomain() = Room(
    id = roomID,
    name = roomName,
    type = roomType,
    price = price,
    capacity = maxPeople,
    status = status,
    imageUrl = firstImage,
    address = address
)