package com.dabi.opensky.core.data.remote.model.response

import com.dabi.opensky.core.data.remote.model.Room
import com.squareup.moshi.Json

data class RoomResponse (
    @Json(name = "rooms")
    val listRoom: List<Room>,
    @Json(name = "currentPage")
    val currentPage: Int,
    @Json(name = "pageSize")
    val pageSize: Int,
    @Json(name = "totalRooms")
    val totalRooms: Int,
    @Json(name = "totalPages")
    val totalPages: Int,
    @Json(name = "hasNextPage")
    val hasNextPage: Boolean,
    @Json(name = "hasPreviousPage")
    val hasPreviousPage: Boolean
)