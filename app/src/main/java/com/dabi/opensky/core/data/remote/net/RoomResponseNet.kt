package com.dabi.opensky.core.data.remote.net

data class RoomResponseNet(
    val rooms: List<RoomNet> = emptyList(),
    val currentPage: Int = 1,
    val pageSize: Int = 10,
    val totalRooms: Int = 0,
    val totalPages: Int = 0,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)