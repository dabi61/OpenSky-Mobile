package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.model.ProvincesResponse
import com.dabi.opensky.core.model.hotel.Hotel
import com.dabi.opensky.core.model.hotel.HotelDetailResponse
import com.dabi.opensky.core.model.hotel.HotelSearchResponse
import com.dabi.opensky.core.model.hotel.HotelsByProvinceResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Hotel API interface - Token sẽ được tự động thêm bởi AuthInterceptor
 * Base URL: https://opensky-be-production.up.railway.app/
 */
interface HotelService {

    @GET("hotels/{hotelId}")
    suspend fun getHotelById(@Path("hotelId") hotelId: String): Response<HotelDetailResponse>
    
    /**
     * Get all hotels (without search filters)
     * GET /hotels
     */
    @GET("hotels/search")
    suspend fun getAllHotels(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("sortBy") sortBy: String = "name",
        @Query("sortOrder") sortOrder: String = "asc"
    ): Response<HotelSearchResponse>
    
    /**
     * Get featured/popular hotels - Using regular search for now
     * GET /hotels
     */
    @GET("hotels")
    suspend fun getFeaturedHotels(
        @Query("limit") limit: Int = 5,
        @Query("page") page: Int = 1
    ): Response<HotelSearchResponse>
    

    /** Search theo keyword */
    @GET("hotels/search")
    suspend fun searchHotels(
        @Query("q") keyword: String?,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<HotelSearchResponse> // (giữ model chuẩn cũ nếu bạn đã có)

    /** Danh sách tỉnh */
    @GET("hotels/provinces")
    suspend fun getProvinces(): Response<ProvincesResponse>

    /** Lấy khách sạn theo tỉnh (chọn từ danh sách provinces) */
    @GET("hotels/province/{province}")
    suspend fun getHotelsByProvince(
        @Path("province") province: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<HotelsByProvinceResponse> // fields: currentPage, pageSize, ...

    /** Lấy khách sạn theo số sao */
    @GET("hotels/star/{stars}")
    suspend fun getHotelsByStars(
        @Path("stars") stars: Int,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<HotelsByProvinceResponse> // cùng shape như theo tỉnh
}
