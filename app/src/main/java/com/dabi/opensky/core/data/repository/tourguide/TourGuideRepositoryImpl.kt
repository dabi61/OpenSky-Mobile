// core/data/repository/tourguide/TourGuideRepositoryImpl.kt
package com.dabi.opensky.core.data.repository.tourguide

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.api.TourGuideService
import com.dabi.opensky.core.data.remote.apiCall
import com.dabi.opensky.core.model.tourguide.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TourGuideRepositoryImpl @Inject constructor(
    private val service: TourGuideService
) : TourGuideRepository {

    override suspend fun getMyAssignments(page: Int, size: Int) =
        apiCall { service.getMyAssignments(page, size) }

    override suspend fun getTourItinerariesByTour(tourId: String) =
        apiCall { service.getTourItinerariesByTour(tourId) }

    override suspend fun getScheduleItinerariesBySchedule(scheduleId: String) =
        apiCall { service.getScheduleItinerariesBySchedule(scheduleId) }

    override suspend fun startItinerary(body: StartItineraryRequest) =
        apiCall { service.startItinerary(body) }

    override suspend fun endItinerary(scheduleItID: String, body: EndItineraryRequest) =
        apiCall { service.endItinerary(scheduleItID, body) }
}
