package com.dabi.opensky.core.data.repository.hotel

import com.dabi.opensky.core.data.remote.api.HotelApi
import com.dabi.opensky.core.model.Hotel
import com.dabi.opensky.core.model.HotelSearchRequest
import com.dabi.opensky.core.model.HotelSearchResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HotelRepository @Inject constructor(
    private val hotelApi: HotelApi // Token được tự động inject bởi AuthInterceptor
) {
    
    /**
     * Search hotels với filters
     */
    suspend fun searchHotels(request: HotelSearchRequest): Result<HotelSearchResponse> {
        return try {
            val response = hotelApi.searchHotels(
                query = request.q,
                province = request.province,
                address = request.address,
                stars = request.stars,
                minPrice = request.minPrice,
                maxPrice = request.maxPrice,
                sortBy = request.sortBy,
                sortOrder = request.sortOrder,
                page = request.page,
                limit = request.limit
            )
            if (response.isSuccessful) {
                response.body()?.let { hotelResponse ->
                    Result.success(hotelResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Search failed: ${response.code()} - ${response.message()} - ${response.raw()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search hotels as Flow cho UI observe
     */
    fun searchHotelsFlow(request: HotelSearchRequest): Flow<Result<HotelSearchResponse>> = flow {
        emit(searchHotels(request))
    }
    
    /**
     * Get hotel by ID
     */
    suspend fun getHotelById(hotelId: String): Result<Hotel> {
        return try {
            val response = hotelApi.getHotelById(hotelId)
            if (response.isSuccessful) {
                response.body()?.let { hotel ->
                    Result.success(hotel)
                } ?: Result.failure(Exception("Hotel not found"))
            } else {
                Result.failure(Exception("Failed to get hotel: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all hotels (for home screen)
     */
    suspend fun getAllHotels(
        page: Int = 1,
        limit: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): Result<HotelSearchResponse> {
        return try {
            val response = hotelApi.getAllHotels(page, limit, sortBy, sortOrder)
            if (response.isSuccessful) {
                response.body()?.let { hotelResponse ->
                    Result.success(hotelResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to get hotels: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all hotels as Flow
     */
    fun getAllHotelsFlow(
        page: Int = 1,
        limit: Int = 10,
        sortBy: String = "name",
        sortOrder: String = "asc"
    ): Flow<Result<HotelSearchResponse>> = flow {
        emit(getAllHotels(page, limit, sortBy, sortOrder))
    }
    
    /**
     * Get featured hotels
     */
    suspend fun getFeaturedHotels(limit: Int = 5): Result<List<Hotel>> {
        return try {
            val response = hotelApi.getFeaturedHotels(limit = limit, page = 1)
            if (response.isSuccessful) {
                response.body()?.let { hotelResponse ->
                    Result.success(hotelResponse.hotels)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to get featured hotels: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get hotels by province
     */
    suspend fun getHotelsByProvince(
        province: String,
        page: Int = 1,
        limit: Int = 10
    ): Result<HotelSearchResponse> {
        return try {
            val response = hotelApi.getHotelsByProvince(province, page, limit)
            if (response.isSuccessful) {
                response.body()?.let { hotelResponse ->
                    Result.success(hotelResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to get hotels by province: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Quick search - tìm kiếm đơn giản chỉ với query
     */
    suspend fun quickSearch(query: String, limit: Int = 10): Result<HotelSearchResponse> {
        val request = HotelSearchRequest(
            q = query,
            limit = limit
        )
        return searchHotels(request)
    }
    
    /**
     * Advanced search - tìm kiếm với nhiều filters
     */
    suspend fun advancedSearch(
        query: String? = null,
        province: String? = null,
        stars: Int? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        page: Int = 1,
        limit: Int = 10
    ): Result<HotelSearchResponse> {
        val request = HotelSearchRequest(
            q = query,
            province = province,
            stars = stars,
            minPrice = minPrice,
            maxPrice = maxPrice,
            page = page,
            limit = limit
        )
        return searchHotels(request)
    }
}
