package com.dabi.opensky.core.model.schedule

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScheduleListResponse(
    @Json(name = "schedules") val schedules: List<ScheduleItem>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "totalPages") val totalPages: Int
)

@JsonClass(generateAdapter = true)
data class ScheduleItem(
    @Json(name = "scheduleID") val scheduleID: String,
    @Json(name = "tourID") val tourID: String,
    @Json(name = "userID") val userID: String,
    @Json(name = "startTime") val startTime: String,   // ISO UTC
    @Json(name = "endTime") val endTime: String,       // ISO UTC
    @Json(name = "numberPeople") val numberPeople: Int,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String?,
    @Json(name = "tourName") val tourName: String,
    @Json(name = "user") val user: ScheduleUser?,
    @Json(name = "tour") val tour: ScheduleTour?
)

/** API /schedules/{scheduleId} trả cùng shape với item → có thể dùng lại */
typealias ScheduleDetailResponse = ScheduleItem

@JsonClass(generateAdapter = true)
data class ScheduleUser(
    @Json(name = "userID") val userID: String,
    @Json(name = "email") val email: String?,
    @Json(name = "fullName") val fullName: String?,
    @Json(name = "phoneNumber") val phoneNumber: String?,
    @Json(name = "citizenId") val citizenId: String?
)

@JsonClass(generateAdapter = true)
data class ScheduleTour(
    @Json(name = "tourID") val tourID: String,
    @Json(name = "tourName") val tourName: String,
    @Json(name = "description") val description: String?,
    @Json(name = "maxPeople") val maxPeople: Int,
    @Json(name = "price") val price: Long,
    @Json(name = "star") val star: Int,
    @Json(name = "imageUrl") val imageUrl: String?
)
