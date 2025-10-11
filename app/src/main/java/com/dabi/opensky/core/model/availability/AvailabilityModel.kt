// core/model/booking/AvailabilityModels.kt
package com.dabi.opensky.core.model.availability

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AvailabilityRequest(
    @Json(name = "roomID") val roomID: String,
    // khoảng muốn kiểm tra (có thể cả 1 tháng)
    @Json(name = "checkInDate") val fromDate: String,  // ISO-8601 UTC, vd: 2025-10-01T00:00:00Z
    @Json(name = "checkOutDate") val toDate: String       // ISO-8601 UTC, vd: 2025-10-31T00:00:00Z
)

@JsonClass(generateAdapter = true)
data class AvailabilityResponse(
    @Json(name = "isAvailable") val isAvailable: Boolean,
    @Json(name = "message") val message: String?,
    @Json(name = "conflicts") val conflicts: List<BookingConflict> = emptyList(),
    @Json(name = "price") val price: Long? = null,
    @Json(name = "numberOfNights") val numberOfNights: Int? = null,
    @Json(name = "totalPrice") val totalPrice: Long? = null
)

@JsonClass(generateAdapter = true)
data class BookingConflict(
    @Json(name = "bookingId") val bookingId: String,
    @Json(name = "checkInDate") val checkInDate: String,
    @Json(name = "checkOutDate") val checkOutDate: String,
    @Json(name = "status") val status: String
)
