package com.dabi.opensky.core.data.repository.room

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.room.CreateRoomResponse
import com.dabi.opensky.core.model.room.Room
import com.dabi.opensky.core.model.room.RoomDetailResponse
import com.dabi.opensky.core.model.room.RoomsByStatusResponse
import com.dabi.opensky.core.model.room.RoomsResponse
import com.dabi.opensky.core.model.room.SimpleMessageResponse
import com.dabi.opensky.core.model.room.UpdateRoomResponse
import kotlinx.coroutines.flow.Flow
import java.io.File

interface RoomRepository {
    suspend fun getRoomsByHotel(hotelId: String, page: Int, limit: Int): Flow<Resource<Pair<List<Room>, RoomsResponse>>>
    suspend fun getRoomDetail(roomId: String): Flow<Resource<RoomDetailResponse>>

    suspend fun getRoomsByHotelAndStatus(hotelId: String, status: String, page: Int = 1, limit: Int = 50): Flow<Resource<RoomsByStatusResponse>>

    suspend fun createRoom(
        hotelId: String,
        roomName: String,
        roomType: String,
        address: String,
        price: Double,
        maxPeople: Int,
        files: List<File> = emptyList()
    ): Flow<Resource<CreateRoomResponse>>

    suspend fun updateRoom(
        roomId: String,
        roomName: String? = null,
        roomType: String? = null,
        address: String? = null,
        price: Double? = null,
        maxPeople: Int? = null,
        existingImageIdsCsv: String? = null,
        deleteImageIdsCsv: String? = null,
        files: List<File> = emptyList()
    ): Flow<Resource<UpdateRoomResponse>>

    suspend fun updateRoomStatus(roomId: String, status: String): Flow<Resource<SimpleMessageResponse>>
    suspend fun deleteRoom(roomId: String): Flow<Resource<SimpleMessageResponse>>
}