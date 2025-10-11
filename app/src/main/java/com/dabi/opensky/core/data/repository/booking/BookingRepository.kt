package com.dabi.opensky.core.data.repository.booking

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.availability.AvailabilityRequest
import com.dabi.opensky.core.model.availability.AvailabilityResponse
import com.dabi.opensky.core.model.availability.BookingConflict
import com.dabi.opensky.core.model.booking.BookingRequest
import com.dabi.opensky.core.model.booking.BookingResponse
import com.dabi.opensky.core.model.booking.HotelBookingDetail
import com.dabi.opensky.core.model.booking.MyBookingsResponse
import com.dabi.opensky.core.model.booking.SearchBookingsResponse
import com.dabi.opensky.core.model.booking.SimpleMessageResponse
import com.dabi.opensky.core.model.booking.TourBookingDetail
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun createBooking(body: BookingRequest): Flow<Resource<BookingResponse>>

    fun getMyBookings(page: Int, limit: Int): Flow<Resource<MyBookingsResponse>>

    fun getHotelBookingDetail(bookingId: String): Flow<Resource<HotelBookingDetail>>

    fun searchBookings(
        page: Int, limit: Int,
        query: String? = null,
        status: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        hotelId: String? = null,
        tourId: String? = null,
        type: String? = null
    ): Flow<Resource<SearchBookingsResponse>>

    fun checkIn(bookingId: String): Flow<Resource<SimpleMessageResponse>>

    fun checkOut(bookingId: String): Flow<Resource<SimpleMessageResponse>>
    // new
    fun checkAvailability(body: AvailabilityRequest): Flow<Resource<AvailabilityResponse>>
    fun getMonthAvailability(roomId: String, year: Int, month: Int): Flow<Resource<MonthAvailability>>
    fun getTourBookingDetail(bookingId: String): Flow<Resource<TourBookingDetail>>
}

data class MonthAvailability(
    val year: Int,
    val month: Int, // 1..12
    val conflicts: List<BookingConflict>,
    val bookedDates: Set<java.time.LocalDate> // các ngày đã dính booking (đếm theo đêm)
)