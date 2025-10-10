package com.dabi.opensky.core.model.hotel

import com.dabi.opensky.core.model.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Hotel(
    @Json(name = "hotelID")
    val hotelID: String,

    @Json(name = "userID")
    val userID: String? = null,

    @Json(name = "hotelName")
    val hotelName: String,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "address")
    val address: String,

    @Json(name = "province")
    val province: String,

    @Json(name = "latitude")
    val latitude: Double? = null,

    @Json(name = "longitude")
    val longitude: Double? = null,

    @Json(name = "star")
    val star: Int,

    @Json(name = "status")
    val status: String? = null,

    @Json(name = "createdAt")
    val createdAt: String,

    @Json(name = "images")
    val images: List<String> = emptyList(),

    @Json(name = "firstImage")
    val firstImage: String? = null,

    @Json(name = "user")
    val user: User? = null,
    
    // Backward compatibility fields - có thể sẽ được thêm từ API khác
    @Json(name = "minPrice")
    val minPrice: Double? = null,

    @Json(name = "maxPrice")
    val maxPrice: Double? = null,

    @Json(name = "totalRooms")
    val totalRooms: Int? = null,

    @Json(name = "availableRooms")
    val availableRooms: Int? = null,
) {
    // Helper properties để backward compatibility
    val displayImage: String?
        get() = firstImage ?: images.firstOrNull()
        
    val ownerName: String?
        get() = user?.fullName
        
    val displayMinPrice: Double
        get() = minPrice ?: 0.0
        
    val displayMaxPrice: Double  
        get() = maxPrice ?: 0.0
        
    val displayTotalRooms: Int
        get() = totalRooms ?: 0
        
    val displayAvailableRooms: Int
        get() = availableRooms ?: 0
}

@JsonClass(generateAdapter = true)
data class images(
    @Json(name = "imageId")
    val imageId: Int,
    @Json(name = "imageUrl")
    val image: String
)