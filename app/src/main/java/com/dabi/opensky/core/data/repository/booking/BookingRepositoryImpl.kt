package com.dabi.opensky.core.data.repository.booking

import android.os.Build
import androidx.annotation.RequiresApi
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.api.BookingService
import com.dabi.opensky.core.data.remote.apiCall
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
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    private val api: BookingService
) : BookingRepository {
    override fun createBooking(body: BookingRequest): Flow<Resource<BookingResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.createBooking(body) })
    }

    override fun getMyBookings(page: Int, limit: Int): Flow<Resource<MyBookingsResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.getMyBookings(page, limit) })
    }

    override fun getHotelBookingDetail(bookingId: String): Flow<Resource<HotelBookingDetail>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.getHotelBookingDetail(bookingId) })
    }

    override fun searchBookings(
        page: Int, limit: Int,
        query: String? ,
        status: String? ,
        fromDate: String?,
        toDate: String? ,
        hotelId: String?,
        tourId: String?,
        type: String?
    ): Flow<Resource<SearchBookingsResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.searchBookings(page, limit, query, status, fromDate, toDate, hotelId, tourId, type) })
    }

    override fun checkIn(bookingId: String): Flow<Resource<SimpleMessageResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.checkIn(bookingId) })
    }

    override fun checkOut(bookingId: String): Flow<Resource<SimpleMessageResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.checkOut(bookingId) })
    }
    /* ------------ Availability ------------ */
    override fun checkAvailability(body: AvailabilityRequest): Flow<Resource<AvailabilityResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.checkAvailability(body) })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getMonthAvailability(roomId: String, year: Int, month: Int): Flow<Resource<MonthAvailability>> = flow {
        emit(Resource.Loading)

        val firstDay = LocalDate.of(year, month, 1)
        val lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth())
        // khoảng đêm: [from, to) → set toDate = lastDay.plusDays(1)
        val zone = ZoneId.of("UTC")
        val fromIso = firstDay.atStartOfDay(zone).toInstant().toString()
        val toIso = lastDay.plusDays(1).atStartOfDay(zone).toInstant().toString()

        val req = AvailabilityRequest(roomID = roomId, fromDate = fromIso, toDate = toIso)
        when (val res = apiCall { api.checkAvailability(req) }) {
            is Resource.Success -> {
                val booked = conflictsToDates(res.data.conflicts)
                emit(Resource.Success(MonthAvailability(year, month, res.data.conflicts, booked)))
            }
            is Resource.Error -> emit(res)
            is Resource.Loading -> { /* no-op */ }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun conflictsToDates(conflicts: List<BookingConflict>): Set<LocalDate> {
        val set = linkedSetOf<LocalDate>()
        conflicts.forEach { c ->
            // booking theo đêm: [checkIn, checkOut)
            val inDate = runCatching { Instant.parse(c.checkInDate).atZone(ZoneId.of("UTC")).toLocalDate() }.getOrNull()
            val outDate = runCatching { Instant.parse(c.checkOutDate).atZone(ZoneId.of("UTC")).toLocalDate() }.getOrNull()
            if (inDate != null && outDate != null) {
                var d = inDate
                while (d!!.isBefore(outDate)) { // không tô ngày trả phòng
                    set += d
                    d = d.plusDays(1)
                }
            }
        }
        return set
    }
    override fun getTourBookingDetail(bookingId: String): Flow<Resource<TourBookingDetail>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.getTourBookingDetail(bookingId) })
    }
}