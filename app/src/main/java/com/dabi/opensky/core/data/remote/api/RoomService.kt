package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.data.remote.model.response.RoomResponse
import com.dabi.opensky.core.data.remote.net.RoomResponseNet
import com.dabi.opensky.core.model.hotel.Hotel
import com.dabi.opensky.core.model.hotel.HotelSearchResponse
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
    suspend fun getAllRoomById(
        @Path("hotelId") hotelId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
    ): Response<RoomResponseNet>

}

