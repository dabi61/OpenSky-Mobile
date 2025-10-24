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


@JsonClass(generateAdapter = true)
data class HotelUpdateResponse(
    @Json(name = "message") val message: String?,
    @Json(name = "existingImageUrls") val existingImageUrls: List<String> = emptyList(),
    @Json(name = "newImageUrls") val newImageUrls: List<String> = emptyList(),
    @Json(name = "deletedImageUrls") val deletedImageUrls: List<String> = emptyList(),
    @Json(name = "failedUploads") val failedUploads: List<String> = emptyList(),
    @Json(name = "existingImageCount") val existingImageCount: Int = 0,
    @Json(name = "newImageCount") val newImageCount: Int = 0,
    @Json(name = "deletedImageCount") val deletedImageCount: Int = 0,
    @Json(name = "failedImageCount") val failedImageCount: Int = 0,
    @Json(name = "totalImageCount") val totalImageCount: Int = 0
)