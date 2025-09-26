package com.dabi.opensky.core.model.room


data class Room(
    val roomID: String,
    val hotelID: String,
    val hotelName: String,
    val roomName: String,
    val roomType: String?,
    val address: String?,
    val price: Double,
    val maxPeople: Int?,
    val status: String?,
    val createdAt: String?,
    val firstImage: String?,
    val images: List<String> = listOfNotNull(firstImage)
)

data class RoomsPage(
    val rooms: List<Room> = emptyList(),
    val currentPage: Int = 1,
    val pageSize: Int = 10,
    val totalRooms: Int = 0,
    val totalPages: Int = 0,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

/* =====================================================
* REPOSITORY CONTRACT (implement in your data layer)
* ===================================================== */



/* =====================================================
* DATA LAYER — Repo impl for RoomService (paging list)
* ===================================================== */

// Network DTOs matching your JSON response (kept local for clarity; move to remote module if needed)
private data class RoomResponseNet(
    val rooms: List<RoomNet> = emptyList(),
    val currentPage: Int = 1,
    val pageSize: Int = 10,
    val totalRooms: Int = 0,
    val totalPages: Int = 0,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

private data class RoomNet(
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

private fun RoomNet.toDomain(): Room = Room(
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