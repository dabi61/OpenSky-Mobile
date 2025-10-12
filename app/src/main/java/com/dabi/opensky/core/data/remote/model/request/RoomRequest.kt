package com.dabi.opensky.core.data.remote.model.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RoomRequest (
    @Json(name = "hotelID")
    val hotelID: String,

    @Json(name = "page")
    val page: Int? = null,

    @Json(name = "limit")
    val limit: Int? = null,
)

