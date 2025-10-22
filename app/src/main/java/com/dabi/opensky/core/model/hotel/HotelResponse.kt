package com.dabi.opensky.core.model.hotel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HotelSearchResponse(
    @Json(name = "hotels")
    val hotels: List<Hotel>,
    @Json(name = "totalCount")
    val totalCount: Int,
    @Json(name = "page")
    val page: Int,
    @Json(name = "limit")
    val limit: Int,
    @Json(name = "totalPages")
    val totalPages: Int,
    @Json(name = "hasNextPage")
    val hasNextPage: Boolean,
    @Json(name = "hasPreviousPage")
    val hasPreviousPage: Boolean
)

@JsonClass(generateAdapter = true)
// core/model/hotel/HotelSearchRequest.kt
data class HotelSearchRequest(
    val q: String? = null,
    val page: Int = 1,
    val limit: Int = 20
)
