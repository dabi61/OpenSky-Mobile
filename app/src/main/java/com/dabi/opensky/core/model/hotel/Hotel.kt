package com.dabi.opensky.core.model.hotel

import com.dabi.opensky.core.model.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/* --------- Hotel (dùng cho hiển thị chung) --------- */
@JsonClass(generateAdapter = true)
data class Hotel(
    @Json(name = "hotelID") val hotelID: String,
    @Json(name = "userID") val userID: String? = null,
    @Json(name = "hotelName") val hotelName: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "address") val address: String,
    @Json(name = "province") val province: String,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "star") val star: Int,
    @Json(name = "status") val status: String? = null,
    @Json(name = "createdAt") val createdAt: String,

    // URL ảnh để hiển thị
    @Json(name = "images") val images: List<String> = emptyList(),
    @Json(name = "firstImage") val firstImage: String? = null,

    @Json(name = "user") val user: User? = null,

    // Optional fields từ API khác
    @Json(name = "minPrice") val minPrice: Double? = null,
    @Json(name = "maxPrice") val maxPrice: Double? = null,
    @Json(name = "totalRooms") val totalRooms: Int? = null,
    @Json(name = "availableRooms") val availableRooms: Int? = null,
) {
    val displayImage: String? get() = firstImage ?: images.firstOrNull()
    val ownerName: String? get() = user?.fullName
    val displayMinPrice: Double get() = minPrice ?: 0.0
    val displayMaxPrice: Double get() = maxPrice ?: 0.0
    val displayTotalRooms: Int get() = totalRooms ?: 0
    val displayAvailableRooms: Int get() = availableRooms ?: 0
}

/* ⚠️ XÓA class cũ `images` (viết thường) – không dùng và dễ nhầm */

/* --------- MyHotel (dùng cho flow Owner) --------- */
@JsonClass(generateAdapter = true)
data class MyHotelImageDto(
    @Json(name = "imageId") val imageId: Int,          // dùng Int cho đồng bộ với VM
    @Json(name = "imageUrl") val imageUrl: String
)

@JsonClass(generateAdapter = true)
data class MyHotelResponse(
    @Json(name = "hotelID") val hotelID: String,
    @Json(name = "userID") val userID: String? = null,
    @Json(name = "userEmail") val userEmail: String? = null,
    @Json(name = "userFullName") val userFullName: String? = null,
    @Json(name = "hotelName") val hotelName: String,
    @Json(name = "address") val address: String? = null,
    @Json(name = "province") val province: String? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "star") val star: Int = 0,
    @Json(name = "status") val status: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "images") val images: List<MyHotelImageDto> = emptyList()
)

/* Map sang Hotel để tái dùng UI hiện có */
fun MyHotelResponse.toHotel(): Hotel {
    val urls = images.map { it.imageUrl }.filter { it.isNotBlank() }
    return Hotel(
        hotelID = hotelID,
        userID = userID,
        hotelName = hotelName,
        description = description,
        address = address.orEmpty(),
        province = province.orEmpty(),
        latitude = latitude,
        longitude = longitude,
        star = star,
        status = status,
        createdAt = createdAt.orEmpty(),
        images = urls,
        firstImage = urls.firstOrNull(),
        user = null,
        minPrice = null,
        maxPrice = null,
        totalRooms = null,
        availableRooms = null
    )
}

/* --------- Payload & Response cho PUT /hotels/{hotelId} --------- */
data class HotelUpdatePayload(
    val hotelName: String? = null,
    val description: String? = null,
    val address: String? = null,
    val province: String? = null,
    val latitude: Double? = null,   // VM sẽ convert -> String khi build multipart
    val longitude: Double? = null,
    /** ID ảnh muốn GIỮ LẠI */
    val existingImageIds: List<Int>? = null,  // dùng Int đồng bộ với DTO/VM
    /** ID ảnh muốn XOÁ */
    val deleteImageIds: List<Int>? = null,
    /** Ảnh mới (file) */
    val newImageFiles: List<java.io.File> = emptyList()
)

@JsonClass(generateAdapter = true)
data class OwnerHotelUpdateResponse(
    @Json(name = "message") val message: String?,
    @Json(name = "existingImageUrls") val existingImageUrls: List<String>?,
    @Json(name = "newImageUrls") val newImageUrls: List<String>?,
    @Json(name = "deletedImageUrls") val deletedImageUrls: List<String>?,
    @Json(name = "failedUploads") val failedUploads: List<String>?,
    @Json(name = "existingImageCount") val existingImageCount: Int?,
    @Json(name = "newImageCount") val newImageCount: Int?,
    @Json(name = "deletedImageCount") val deletedImageCount: Int?,
    @Json(name = "failedImageCount") val failedImageCount: Int?,
    @Json(name = "totalImageCount") val totalImageCount: Int?
)
