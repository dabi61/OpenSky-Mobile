package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.model.schedule.ScheduleDetailResponse
import com.dabi.opensky.core.model.schedule.ScheduleListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleService {
    @GET("schedules/tour/{tourId}")
    suspend fun getSchedulesByTour(
        @Path("tourId") tourId: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ScheduleListResponse>

    @GET("schedules/{scheduleId}")
    suspend fun getScheduleDetail(
        @Path("scheduleId") scheduleId: String
    ): Response<ScheduleDetailResponse>
}
