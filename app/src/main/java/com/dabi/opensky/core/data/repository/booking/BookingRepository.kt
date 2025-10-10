package com.dabi.opensky.core.data.repository.booking

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.booking.BookingRequest
import com.dabi.opensky.core.model.booking.BookingResponse
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    suspend fun createBooking(body: BookingRequest): Flow<Resource<BookingResponse>>
}