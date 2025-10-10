package com.dabi.opensky.core.model.booking

// core/model/booking/BookingModels.kt

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookingRoomRef(@Json(name = "roomID") val roomID: String)

@JsonClass(generateAdapter = true)
data class BookingRequest(
    @Json(name = "rooms") val rooms: List<BookingRoomRef>,
    @Json(name = "checkInDate") val checkInDate: String,   // ISO-8601
    @Json(name = "checkOutDate") val checkOutDate: String  // ISO-8601
)

/** Tuỳ backend trả gì, để generic */
@JsonClass(generateAdapter = true)
data class BookingResponse(
    @Json(name = "bookingId") val bookingId: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null
)
