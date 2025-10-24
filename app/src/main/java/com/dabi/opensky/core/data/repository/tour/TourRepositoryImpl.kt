package com.dabi.opensky.core.data.repository.tour

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.api.TourService
import com.dabi.opensky.core.data.remote.apiCall
import com.dabi.opensky.core.model.tour.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TourRepositoryImpl @Inject constructor(
    private val api: TourService
) : TourRepository {

    override suspend fun getTours(
        page: Int,
        size: Int
    ): Flow<Resource<Pair<List<Tour>, ToursResponse>>> = flow {
        emit(Resource.Loading)
        when (val res = apiCall { api.getTours(page, size) }) {
            is Resource.Success -> {
                val body = res.data
                emit(Resource.Success(body.tours.map { it.toDomain() } to body))
            }
            is Resource.Error   -> emit(res)
            is Resource.Loading -> Unit
        }
    }

    override suspend fun getTourDetail(tourId: String): Flow<Resource<TourDetailResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.getTourDetail(tourId) })
    }
}
