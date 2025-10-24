// core/data/repository/room/RoomRepositoryImpl.kt
package com.dabi.opensky.core.data.repository.room

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.api.RoomService
import com.dabi.opensky.core.data.remote.apiCall
import com.dabi.opensky.core.model.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val api: RoomService
) : RoomRepository {

    override suspend fun getRoomsByHotel(
        hotelId: String, page: Int, limit: Int
    ): Flow<Resource<Pair<List<Room>, RoomsResponse>>> = flow {
        emit(Resource.Loading)
        when (val res = apiCall { api.getRoomsByHotel(hotelId, page, limit) }) {
            is Resource.Success -> emit(Resource.Success(res.data.rooms.map { it.toDomain() } to res.data))
            is Resource.Error -> emit(res)
            is Resource.Loading -> Unit
        }
    }

    override suspend fun getRoomDetail(roomId: String): Flow<Resource<RoomDetailResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.getRoomDetail(roomId) })
    }

    override suspend fun getRoomsByHotelAndStatus(
        hotelId: String, status: String, page: Int, limit: Int
    ): Flow<Resource<RoomsByStatusResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.getRoomsByHotelAndStatus(hotelId, status, page, limit) })
    }

    override suspend fun createRoom(
        hotelId: String, roomName: String, roomType: String, address: String,
        price: Double, maxPeople: Int, files: List<File>
    ): Flow<Resource<CreateRoomResponse>> = flow {
        emit(Resource.Loading)

        val text: (String) -> RequestBody = { it.toRequestBody("text/plain".toMediaType()) }
        val parts = files.mapIndexed { idx, f ->
            val body = f.asRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("files", f.name, body)
        }

        emit(apiCall {
            api.createRoom(
                hotelId = text(hotelId),
                roomName = text(roomName),
                roomType = text(roomType),
                address = text(address),
                price = text(price.toString()),
                maxPeople = text(maxPeople.toString()),
                files = parts
            )
        })
    }

    override suspend fun updateRoom(
        roomId: String,
        roomName: String?, roomType: String?, address: String?,
        price: Double?, maxPeople: Int?,
        existingImageIdsCsv: String?, deleteImageIdsCsv: String?,
        files: List<File>
    ): Flow<Resource<UpdateRoomResponse>> = flow {
        emit(Resource.Loading)

        fun textOrNull(v: String?) = v?.toRequestBody("text/plain".toMediaType())
        val parts = files.map { f ->
            val body = f.asRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("files", f.name, body)
        }

        emit(apiCall {
            api.updateRoom(
                roomId = roomId,
                roomName = textOrNull(roomName),
                roomType = textOrNull(roomType),
                address = textOrNull(address),
                price = textOrNull(price?.toString()),
                maxPeople = textOrNull(maxPeople?.toString()),
                existingImageIdsCsv = textOrNull(existingImageIdsCsv),
                deleteImageIdsCsv = textOrNull(deleteImageIdsCsv),
                files = parts
            )
        })
    }

    override suspend fun updateRoomStatus(
        roomId: String, status: String
    ): Flow<Resource<SimpleMessageResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.updateRoomStatus(roomId, UpdateRoomStatusRequest(status)) })
    }

    override suspend fun deleteRoom(roomId: String): Flow<Resource<SimpleMessageResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { api.deleteRoom(roomId) })
    }
}
