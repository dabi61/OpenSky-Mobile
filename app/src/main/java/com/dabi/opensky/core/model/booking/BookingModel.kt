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

@JsonClass(generateAdapter = true)
data class MyBookingsResponse(
    @Json(name = "hotelBookings") val hotelBookings: List<HotelBookingSummary>,
    @Json(name = "tourBookings") val tourBookings: List<TourBookingSummary>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "totalPages") val totalPages: Int
)

@JsonClass(generateAdapter = true)
data class HotelBookingSummary(
    @Json(name = "bookingID") val bookingID: String,
    @Json(name = "hotelName") val hotelName: String,
    @Json(name = "checkInDate") val checkInDate: String,
    @Json(name = "checkOutDate") val checkOutDate: String,
    @Json(name = "status") val status: String,            // Pending/Confirmed/Completed/Cancelled...
    @Json(name = "paymentStatus") val paymentStatus: String?, // Paid/...
    @Json(name = "billID") val billID: String?,
    @Json(name = "createdAt") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class TourBookingSummary(
    @Json(name = "bookingID") val bookingID: String,
    @Json(name = "userID") val userID: String?,
    @Json(name = "userName") val userName: String?,
    @Json(name = "tourID") val tourID: String,
    @Json(name = "tourName") val tourName: String,
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "numberOfGuests") val numberOfGuests: Int?,
    @Json(name = "status") val status: String,
    @Json(name = "paymentStatus") val paymentStatus: String?,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String?
)

/* --------- Booking detail (hotel) --------- */
@JsonClass(generateAdapter = true)
data class HotelBookingDetail(
    @Json(name = "bookingID") val bookingID: String,
    @Json(name = "userID") val userID: String,
    @Json(name = "userName") val userName: String,
    @Json(name = "userEmail") val userEmail: String,
    @Json(name = "hotelID") val hotelID: String,
    @Json(name = "hotelName") val hotelName: String,
    @Json(name = "hotelAddress") val hotelAddress: String,
    @Json(name = "checkInDate") val checkInDate: String,
    @Json(name = "checkOutDate") val checkOutDate: String,
    @Json(name = "numberOfNights") val numberOfNights: Int,
    @Json(name = "status") val status: String,
    @Json(name = "notes") val notes: String?,
    @Json(name = "paymentMethod") val paymentMethod: String?,
    @Json(name = "paymentStatus") val paymentStatus: String?,
    @Json(name = "billID") val billID: String?,
    @Json(name = "totalPrice") val totalPrice: Long,
    @Json(name = "deposit") val deposit: Long,
    @Json(name = "billStatus") val billStatus: String?,
    @Json(name = "roomDetails") val roomDetails: List<RoomDetailLine>,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class RoomDetailLine(
    @Json(name = "roomID") val roomID: String,
    @Json(name = "roomName") val roomName: String,
    @Json(name = "roomType") val roomType: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unitPrice") val unitPrice: Long,
    @Json(name = "totalPrice") val totalPrice: Long,
    @Json(name = "notes") val notes: String?
)

/* --------- Search bookings --------- */
@JsonClass(generateAdapter = true)
data class SearchBookingsResponse(
    @Json(name = "hotelBookings") val hotelBookings: List<HotelBookingSummary>,
    @Json(name = "tourBookings") val tourBookings: List<TourBookingSummary>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "totalPages") val totalPages: Int
)

/* --------- Common message (check-in/out) --------- */
@JsonClass(generateAdapter = true)
data class SimpleMessageResponse(
    @Json(name = "message") val message: String
)

