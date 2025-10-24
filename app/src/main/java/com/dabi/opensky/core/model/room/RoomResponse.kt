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
    val type: String?,
    val price: Long?,          // <- nullable
    val capacity: Int?,        // <- nullable
    val status: String?,
    val imageUrl: String?,
    val address: String?
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


@JsonClass(generateAdapter = true)
data class CreateRoomResponse(
    @Json(name = "roomID") val roomID: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "uploadedImageUrls") val uploadedImageUrls: List<String>?,
    @Json(name = "failedUploads") val failedUploads: List<String>?,
    @Json(name = "successImageCount") val successImageCount: Int?,
    @Json(name = "failedImageCount") val failedImageCount: Int?
)

@JsonClass(generateAdapter = true)
data class UpdateRoomResponse(
    @Json(name = "message") val message: String?,
    @Json(name = "existingImageUrls") val existingImageUrls: List<String>?,
    @Json(name = "newImageUrls") val newImageUrls: List<String>?,
    @Json(name = "deletedImageUrls") val deletedImageUrls: List<String>?,
    @Json(name = "failedUploads") val failedUploads: List<String>?,
    @Json(name = "existingImageCount") val existingImageCount: Int?,
    @Json(name = "newImageCount") val newImageCount: Int?,
    @Json(name = "deletedImageCount") val deletedImageCount: Int?,
    @Json(name = "failedImageCount") val failedImageCount: Int?,
    @Json(name = "totalImageCount") val totalImageCount: Int?,
    @Json(name = "uploadedImageUrls") val uploadedImageUrls: List<String>?,
    @Json(name = "successImageCount") val successImageCount: Int?,
    @Json(name = "imageAction") val imageAction: String?
)

@JsonClass(generateAdapter = true)
data class UpdateRoomStatusRequest(
    @Json(name = "status") val status: String // Available, Occupied, Maintenance, Removed
)

@JsonClass(generateAdapter = true)
data class SimpleMessageResponse(
    @Json(name = "message") val message: String?
)

/** Response filter rooms theo status */
@JsonClass(generateAdapter = true)
data class RoomsByStatusResponse(
    @Json(name = "rooms") val rooms: List<RoomStatusItem>,
    @Json(name = "totalRooms") val totalRooms: Int,
    @Json(name = "availableRooms") val availableRooms: Int,
    @Json(name = "occupiedRooms") val occupiedRooms: Int,
    @Json(name = "maintenanceRooms") val maintenanceRooms: Int,
    @Json(name = "removedRooms") val removedRooms: Int
)

@JsonClass(generateAdapter = true)
data class RoomStatusItem(
    @Json(name = "roomID") val roomID: String,
    @Json(name = "hotelID") val hotelID: String,
    @Json(name = "hotelName") val hotelName: String?,
    @Json(name = "roomName") val roomName: String,
    @Json(name = "roomType") val roomType: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "notes") val notes: String?,
    @Json(name = "updatedAt") val updatedAt: String?
)