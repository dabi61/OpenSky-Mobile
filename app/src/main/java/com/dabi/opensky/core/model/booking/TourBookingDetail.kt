// core/model/booking/TourBookingDetail.kt
package com.dabi.opensky.core.model.booking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TourBookingDetail(
    @Json(name = "bookingID") val bookingID: String,
    @Json(name = "userID") val userID: String?,
    @Json(name = "userName") val userName: String?,
    @Json(name = "tourID") val tourID: String,
    @Json(name = "tourName") val tourName: String,
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "numberOfGuests") val numberOfGuests: Int?,
    @Json(name = "status") val status: String,
    @Json(name = "notes") val notes: String?,
    @Json(name = "paymentMethod") val paymentMethod: String?,
    @Json(name = "paymentStatus") val paymentStatus: String?,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String?,
    @Json(name = "tourInfo") val tourInfo: TourInfo
)

@JsonClass(generateAdapter = true)
data class TourInfo(
    @Json(name = "tourID") val tourID: String,
    @Json(name = "tourName") val tourName: String,
    @Json(name = "description") val description: String?,
    @Json(name = "price") val price: Long?,
    @Json(name = "duration") val duration: String?,
    @Json(name = "location") val location: String?
)
