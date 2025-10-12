package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.model.room.RoomDetailResponse
import com.dabi.opensky.core.model.room.RoomsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomService {
    /**
     * Get all rooms hotel
     * GET /rooms/hotel/{hotelId}
     */
    @GET("rooms/hotel/{hotelId}")
    suspend fun getRoomsByHotel(
        @Path("hotelId") hotelId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<RoomsResponse>

    @GET("rooms/{roomId}")
    suspend fun getRoomDetail(@Path("roomId") roomId: String): Response<RoomDetailResponse>
}


