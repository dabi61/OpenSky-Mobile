package com.dabi.opensky.core.data.repository.room

import com.dabi.opensky.core.model.room.RoomsPage


interface RoomRepository {
    suspend fun getRoomsOfHotel(hotelId: String, page: Int, pageSize: Int): RoomsPage
//    suspend fun getRoomDetail(roomId: String): RoomDetail
}