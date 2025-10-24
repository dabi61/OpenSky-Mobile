package com.dabi.opensky.core.data.repository.schedule

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.model.schedule.ScheduleDetailResponse
import com.dabi.opensky.core.model.schedule.ScheduleListResponse

interface ScheduleRepository {
    suspend fun getSchedulesByTour(
        tourId: String,
        page: Int,
        size: Int
    ): Resource<ScheduleListResponse>

    suspend fun getScheduleDetail(
        scheduleId: String
    ): Resource<ScheduleDetailResponse>
}
