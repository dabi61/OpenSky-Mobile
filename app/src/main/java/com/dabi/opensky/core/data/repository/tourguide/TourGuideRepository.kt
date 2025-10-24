// core/data/repository/tourguide/TourGuideRepository.kt
package com.dabi.opensky.core.data.repository.tourguide

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.tourguide.*

interface TourGuideRepository {
    suspend fun getMyAssignments(page: Int, size: Int): Resource<MyAssignmentsResponse>
    /** MỚI: theo tourId */
    // A: theo tour
    suspend fun getTourItinerariesByTour(tourId: String): Resource<List<TourItinerary>>

    // B: theo schedule (để lấy trạng thái start/end)
    suspend fun getScheduleItinerariesBySchedule(scheduleId: String): Resource<List<ScheduleItineraryItem>>

    suspend fun startItinerary(body: StartItineraryRequest): Resource<StartItineraryResponse>
    suspend fun endItinerary(scheduleItID: String, body: EndItineraryRequest): Resource<SimpleMessage>
}
