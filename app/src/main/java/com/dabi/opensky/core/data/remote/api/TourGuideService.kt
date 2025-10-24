// core/data/remote/api/TourGuideService.kt
package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.model.tourguide.*
import retrofit2.Response
import retrofit2.http.*

interface TourGuideService {

    @GET("schedules/my-assignments")
    suspend fun getMyAssignments(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<MyAssignmentsResponse>

    // A) Itineraries gốc theo tour
    @GET("tour_itinerary/tour/{tourId}")
    suspend fun getTourItinerariesByTour(
        @Path("tourId") tourId: String
    ): Response<List<TourItinerary>>

    // B) Itineraries đã log trạng thái theo schedule
    @GET("schedule_itinerary/schedule/{scheduleId}")
    suspend fun getScheduleItinerariesBySchedule(
        @Path("scheduleId") scheduleId: String
    ): Response<List<ScheduleItineraryItem>>


    /** Hành động */
    @POST("schedule_itinerary")
    suspend fun startItinerary(@Body body: StartItineraryRequest): Response<StartItineraryResponse>

    @PUT("schedule_itinerary/{scheduleItID}")
    suspend fun endItinerary(
        @Path("scheduleItID") scheduleItID: String,
        @Body body: EndItineraryRequest
    ): Response<SimpleMessage>
}
