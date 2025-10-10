package com.dabi.opensky.core.data.remote.api

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
    
    /**
     * Search hotels với các filter parameters
     * GET /hotels/search
     */
    @GET("hotels/search")
    suspend fun searchHotels(
        @Query("q") query: String? = null,
        @Query("province") province: String? = null,
        @Query("address") address: String? = null,
        @Query("stars") stars: Int? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("sortBy") sortBy: String = "name",
        @Query("sortOrder") sortOrder: String = "asc",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<HotelSearchResponse>
    
    /**
     * Get hotel by ID với thông tin chi tiết và user
     * GET /hotels/{hotelId}
     */
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
    
    /**
     * Get hotels by province ID với pagination mới
     * GET /hotels/province/{provinceId}
     */
    @GET("hotels/province/{provinceId}")
    suspend fun getHotelsByProvinceId(
        @Path("provinceId") provinceId: Int,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<HotelsByProvinceResponse>
    
    /**
     * Get hotels by province name (backward compatibility)
     * GET /hotels/by-province/{province}
     */
    @GET("hotels/by-province/{province}")
    suspend fun getHotelsByProvince(
        @Path("province") province: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<HotelSearchResponse>
}
