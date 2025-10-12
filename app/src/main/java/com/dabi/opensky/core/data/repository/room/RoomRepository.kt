package com.dabi.opensky.core.data.repository.room

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.room.Room
import com.dabi.opensky.core.model.room.RoomDetailResponse
import com.dabi.opensky.core.model.room.RoomsResponse
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    suspend fun getRoomsByHotel(hotelId: String, page: Int, limit: Int): Flow<Resource<Pair<List<Room>, RoomsResponse>>>
    suspend fun getRoomDetail(roomId: String): Flow<Resource<RoomDetailResponse>>
}