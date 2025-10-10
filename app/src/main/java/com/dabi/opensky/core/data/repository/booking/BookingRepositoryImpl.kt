package com.dabi.opensky.core.data.repository.booking

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.api.BookingService
import com.dabi.opensky.core.data.remote.apiCall
import com.dabi.opensky.core.model.booking.BookingRequest
import com.dabi.opensky.core.model.booking.BookingResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    private val api: BookingService
) : BookingRepository {
    override suspend fun createBooking(body: BookingRequest): Flow<Resource<BookingResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.createBooking(body) })
    }
}