// core/data/remote/api/RoomService.kt
package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.model.room.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface RoomService {

    /** Get paginated rooms by hotel */
    @GET("rooms/hotel/{hotelId}")
    suspend fun getRoomsByHotel(
        @Path("hotelId") hotelId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<RoomsResponse>

    @GET("rooms/{roomId}")
    suspend fun getRoomDetail(@Path("roomId") roomId: String): Response<RoomDetailResponse>

    /** Lọc phòng theo trạng thái trong 1 khách sạn */
    @GET("rooms/hotel/{hotelId}/status")
    suspend fun getRoomsByHotelAndStatus(
        @Path("hotelId") hotelId: String,
        @Query("status") status: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<RoomsByStatusResponse>

    /** Tạo phòng mới (multipart) */
    @Multipart
    @POST("rooms")
    suspend fun createRoom(
        @Part("hotelId") hotelId: RequestBody,
        @Part("roomName") roomName: RequestBody,
        @Part("roomType") roomType: RequestBody,
        @Part("address") address: RequestBody,
        @Part("price") price: RequestBody,
        @Part("maxPeople") maxPeople: RequestBody,
        @Part files: List<MultipartBody.Part> = emptyList()
    ): Response<CreateRoomResponse>

    /** Cập nhật phòng + ảnh (multipart) */
    @Multipart
    @PUT("rooms/{roomId}")
    suspend fun updateRoom(
        @Path("roomId") roomId: String,
        @Part("roomName") roomName: RequestBody?,
        @Part("roomType") roomType: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("price") price: RequestBody?,
        @Part("maxPeople") maxPeople: RequestBody?,
        @Part("existingImageIds") existingImageIdsCsv: RequestBody?,  // "1,2,3"
        @Part("deleteImageIds") deleteImageIdsCsv: RequestBody?,      // "4,5"
        @Part files: List<MultipartBody.Part> = emptyList()
    ): Response<UpdateRoomResponse>

    /** Đổi trạng thái phòng */
    @PUT("rooms/{roomId}/status")
    suspend fun updateRoomStatus(
        @Path("roomId") roomId: String,
        @Body req: UpdateRoomStatusRequest
    ): Response<SimpleMessageResponse>

    /** Xoá phòng */
    @DELETE("rooms/{roomId}")
    suspend fun deleteRoom(@Path("roomId") roomId: String): Response<SimpleMessageResponse>
}
