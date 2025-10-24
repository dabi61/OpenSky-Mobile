package com.dabi.opensky.core.data.remote.api


import com.dabi.opensky.core.model.tour.TourDetailResponse
import com.dabi.opensky.core.model.tour.ToursResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TourService {

    // GET /tours?page=1&size=10
    @GET("tours")
    suspend fun getTours(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ToursResponse>

    // GET /tours/detail/{tourId}
    @GET("tours/detail/{tourId}")
    suspend fun getTourDetail(@Path("tourId") tourId: String): Response<TourDetailResponse>
}
