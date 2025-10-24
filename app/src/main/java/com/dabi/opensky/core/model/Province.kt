package com.dabi.opensky.core.model

import com.dabi.opensky.core.model.hotel.Hotel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProvincesResponse(
    @Json(name = "provinces") val provinces: List<String>,
    @Json(name = "totalCount") val totalCount: Int
)

@JsonClass(generateAdapter = true)
data class HotelsByProvinceResponse(
    @Json(name = "hotels") val hotels: List<Hotel>,
    @Json(name = "currentPage") val currentPage: Int,
    @Json(name = "pageSize") val pageSize: Int,
    @Json(name = "totalHotels") val totalHotels: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "hasNextPage") val hasNextPage: Boolean,
    @Json(name = "hasPreviousPage") val hasPreviousPage: Boolean
)