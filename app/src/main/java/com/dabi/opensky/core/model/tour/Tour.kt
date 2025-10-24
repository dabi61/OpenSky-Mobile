package com.dabi.opensky.core.model.tour

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/* ========== LIST ========== */
@JsonClass(generateAdapter = true)
data class ToursResponse(
    @Json(name = "tours") val tours: List<TourItem>,
    @Json(name = "currentPage") val currentPage: Int,
    @Json(name = "pageSize") val pageSize: Int,
    @Json(name = "totalTours") val totalTours: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "hasNextPage") val hasNextPage: Boolean,
    @Json(name = "hasPreviousPage") val hasPreviousPage: Boolean
)

@JsonClass(generateAdapter = true)
data class TourItem(
    @Json(name = "tourID") val tourID: String,
    @Json(name = "tourName") val tourName: String,
    @Json(name = "address") val address: String,
    @Json(name = "province") val province: String,
    @Json(name = "star") val star: Int,
    @Json(name = "price") val price: Long,
    @Json(name = "maxPeople") val maxPeople: Int,
    @Json(name = "status") val status: String,
    @Json(name = "firstImage") val firstImage: String?
)

/* Dùng gọn cho UI list */
data class Tour(
    val id: String,
    val name: String,
    val address: String,
    val province: String,
    val star: Int,
    val price: Long,
    val maxPeople: Int,
    val status: String,
    val imageUrl: String?
)

fun TourItem.toDomain() = Tour(
    id = tourID,
    name = tourName,
    address = address,
    province = province,
    star = star,
    price = price,
    maxPeople = maxPeople,
    status = status,
    imageUrl = firstImage
)

/* ========== DETAIL ========== */
@JsonClass(generateAdapter = true)
data class TourDetailResponse(
    @Json(name = "tourID") val tourID: String,
    @Json(name = "userID") val userID: String?,
    @Json(name = "userName") val userName: String?,
    @Json(name = "userEmail") val userEmail: String?,
    @Json(name = "tourName") val tourName: String,
    @Json(name = "description") val description: String?,
    @Json(name = "address") val address: String,
    @Json(name = "province") val province: String,
    @Json(name = "star") val star: Int,
    @Json(name = "price") val price: Long,
    @Json(name = "maxPeople") val maxPeople: Int,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String?,
    @Json(name = "images") val images: List<TourImageDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TourImageDto(
    @Json(name = "imageId") val imageId: Long?,
    @Json(name = "imageUrl") val imageUrl: String?
)
