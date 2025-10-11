package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.model.availability.AvailabilityRequest
import com.dabi.opensky.core.model.availability.AvailabilityResponse
import com.dabi.opensky.core.model.booking.BookingRequest
import com.dabi.opensky.core.model.booking.BookingResponse
import com.dabi.opensky.core.model.booking.HotelBookingDetail
import com.dabi.opensky.core.model.booking.MyBookingsResponse
import com.dabi.opensky.core.model.booking.SearchBookingsResponse
import com.dabi.opensky.core.model.booking.SimpleMessageResponse
import com.dabi.opensky.core.model.booking.TourBookingDetail
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BookingService {
    @POST("bookings/hotel")
    suspend fun createBooking(@Body body: BookingRequest): Response<BookingResponse>

    @GET("bookings/my-bookings")
    suspend fun getMyBookings(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<MyBookingsResponse>

    @GET("bookings/hotel/{bookingId}/detail")
    suspend fun getHotelBookingDetail(
        @Path("bookingId") bookingId: String
    ): Response<HotelBookingDetail>

    @GET("bookings/search")
    suspend fun searchBookings(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("query") query: String? = null,
        @Query("status") status: String? = null,
        @Query("fromDate") fromDate: String? = null,  // ISO-8601 UTC or date
        @Query("toDate") toDate: String? = null,
        @Query("hotelId") hotelId: String? = null,
        @Query("tourId") tourId: String? = null,
        @Query("type") type: String? = null           // "hotel" / "tour" (nếu backend hỗ trợ)
    ): Response<SearchBookingsResponse>

    /* ---- Actions ---- */
    @PUT("bookings/hotel/{bookingId}/check-in")
    suspend fun checkIn(@Path("bookingId") bookingId: String): Response<SimpleMessageResponse>

    @PUT("bookings/hotel/{bookingId}/check-out")
    suspend fun checkOut(@Path("bookingId") bookingId: String): Response<SimpleMessageResponse>

    /* -------- availability -------- */
    // backend của bạn trả sample theo endpoint này; dùng body để xin cả 1 tháng
    @POST("bookings/hotel/check-availability")
    suspend fun checkAvailability(@Body body: AvailabilityRequest): Response<AvailabilityResponse>

    @GET("bookings/tour/{bookingId}")
    suspend fun getTourBookingDetail(@Path("bookingId") bookingId: String): Response<TourBookingDetail>
}