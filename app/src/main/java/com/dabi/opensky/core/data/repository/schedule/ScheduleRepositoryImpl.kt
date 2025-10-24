package com.dabi.opensky.core.data.repository.schedule

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.api.ScheduleService
import com.dabi.opensky.core.data.remote.apiCall
import com.dabi.opensky.core.model.schedule.ScheduleDetailResponse
import com.dabi.opensky.core.model.schedule.ScheduleListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val service: ScheduleService
) : ScheduleRepository {

    override suspend fun getSchedulesByTour(
        tourId: String,
        page: Int,
        size: Int
    ): Resource<ScheduleListResponse> = apiCall {
        service.getSchedulesByTour(tourId, page, size)
    }

    override suspend fun getScheduleDetail(
        scheduleId: String
    ): Resource<ScheduleDetailResponse> = apiCall {
        service.getScheduleDetail(scheduleId)
    }
}
