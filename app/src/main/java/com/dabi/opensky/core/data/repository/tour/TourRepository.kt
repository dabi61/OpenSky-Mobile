package com.dabi.opensky.core.data.repository.tour


import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.tour.Tour
import com.dabi.opensky.core.model.tour.TourDetailResponse
import kotlinx.coroutines.flow.Flow

interface TourRepository {
    suspend fun getTours(page: Int, size: Int): Flow<Resource<Pair<List<Tour>, com.dabi.opensky.core.model.tour.ToursResponse>>>
    suspend fun getTourDetail(tourId: String): Flow<Resource<TourDetailResponse>>
}