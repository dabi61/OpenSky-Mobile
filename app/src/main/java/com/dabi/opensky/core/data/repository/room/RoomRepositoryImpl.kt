package com.dabi.opensky.core.data.repository.room

import com.dabi.opensky.core.data.remote.api.RoomService
import com.dabi.opensky.core.data.remote.net.toDomain
import com.dabi.opensky.core.model.room.RoomsPage
import retrofit2.HttpException
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val service: RoomService
) : RoomRepository {
    override suspend fun getRoomsOfHotel(hotelId: String, page: Int, pageSize: Int): RoomsPage {
        val res = service.getAllRoomById(hotelId, page, pageSize)
        if (!res.isSuccessful) throw HttpException(res)
        val body = res.body() ?: throw IllegalStateException("Empty body")
        return RoomsPage(
            rooms = body.rooms.map { it.toDomain() },
            currentPage = body.currentPage,
            pageSize = body.pageSize,
            totalRooms = body.totalRooms,
            totalPages = body.totalPages,
            hasNextPage = body.hasNextPage,
            hasPreviousPage = body.hasPreviousPage
        )
    }

//    override suspend fun getRoomDetail(roomId: String): RoomDetail {
// Service hiện tại chưa có API chi tiết phòng → tạm thời không hỗ trợ
//        throw UnsupportedOperationException("getRoomDetail() is not supported by RoomService")
//    }
}