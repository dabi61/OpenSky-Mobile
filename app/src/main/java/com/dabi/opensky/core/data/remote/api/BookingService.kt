package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.model.booking.BookingRequest
import com.dabi.opensky.core.model.booking.BookingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BookingService {
    @POST("bookings/hotel")
    suspend fun createBooking(@Body body: BookingRequest): Response<BookingResponse>
}