// core/data/repository/RoomRepository.kt
package com.dabi.opensky.core.data.repository.room

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.apiCall
import com.dabi.opensky.core.data.remote.api.RoomService
import com.dabi.opensky.core.model.room.Room
import com.dabi.opensky.core.model.room.RoomDetailResponse
import com.dabi.opensky.core.model.room.RoomsResponse
import com.dabi.opensky.core.model.room.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val api: RoomService
): RoomRepository {
    override suspend fun getRoomsByHotel(
        hotelId: String,
        page: Int,
        limit: Int
    ): Flow<Resource<Pair<List<Room>, RoomsResponse>>> = flow {
        emit(Resource.Loading)
        when (val res = apiCall { api.getRoomsByHotel(hotelId, page, limit) }) {
            is Resource.Success -> emit(Resource.Success(res.data.rooms.map { it.toDomain() } to res.data))
            is Resource.Error -> emit(res)
            is Resource.Loading -> emit(res)
        }
    }
    override suspend fun getRoomDetail(roomId: String): Flow<Resource<RoomDetailResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.getRoomDetail(roomId) })
    }
}