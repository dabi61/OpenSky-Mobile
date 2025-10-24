package com.dabi.opensky.core.model.tourguide

// core/model/tourguide/TourGuideModels.kt

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/* -------- My Assignments -------- */
@JsonClass(generateAdapter = true)
data class MyAssignmentsResponse(
    @Json(name = "schedules") val schedules: List<AssignedSchedule>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "totalPages") val totalPages: Int
)

@JsonClass(generateAdapter = true)
data class AssignedSchedule(
    @Json(name = "scheduleID") val scheduleID: String,
    @Json(name = "tourID") val tourID: String,
    @Json(name = "userID") val userID: String,
    @Json(name = "startTime") val startTime: String,
    @Json(name = "endTime") val endTime: String,
    @Json(name = "numberPeople") val numberPeople: Int,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "tourName") val tourName: String,
    @Json(name = "user") val user: GuideUser?,
    @Json(name = "tour") val tour: GuideTour?
)

@JsonClass(generateAdapter = true)
data class GuideUser(
    @Json(name = "userID") val userID: String,
    @Json(name = "email") val email: String?,
    @Json(name = "fullName") val fullName: String?,
    @Json(name = "phoneNumber") val phoneNumber: String?,
    @Json(name = "citizenId") val citizenId: String?
)

@JsonClass(generateAdapter = true)
data class GuideTour(
    @Json(name = "tourID") val tourID: String,
    @Json(name = "tourName") val tourName: String,
    @Json(name = "description") val description: String?,
    @Json(name = "maxPeople") val maxPeople: Int,
    @Json(name = "price") val price: Long,
    @Json(name = "star") val star: Int,
    @Json(name = "imageUrl") val imageUrl: String?
)

/* -------- Schedule Itineraries (list) -------- */
@JsonClass(generateAdapter = true)
data class ScheduleItineraryItem(
    @Json(name = "scheduleItID") val scheduleItID: String,
    @Json(name = "scheduleID") val scheduleID: String,
    @Json(name = "itineraryID") val itineraryID: String,
    @Json(name = "startTime") val startTime: String?,
    @Json(name = "endTime") val endTime: String?,
    @Json(name = "location") val location: String,
    @Json(name = "description") val description: String?,
    @Json(name = "dayNumber") val dayNumber: Int,
    @Json(name = "schedule") val schedule: AssignedSchedule?,
    @Json(name = "tourItinerary") val tourItinerary: TourItinerary?
)

@JsonClass(generateAdapter = true)
data class TourItinerary(
    @Json(name = "itineraryID") val itineraryID: String,
    @Json(name = "tourID") val tourID: String,
    @Json(name = "dayNumber") val dayNumber: Int,
    @Json(name = "location") val location: String,
    @Json(name = "description") val description: String?,
    @Json(name = "isDeleted") val isDeleted: Boolean,
    @Json(name = "tourName") val tourName: String,
    @Json(name = "createdAt") val createdAt: String
)

/* -------- Start / End Itinerary -------- */
@JsonClass(generateAdapter = true)
data class StartItineraryRequest(
    @Json(name = "scheduleID") val scheduleID: String,
    @Json(name = "itineraryID") val itineraryID: String,
    @Json(name = "startTime") val startTime: String // ISO-8601
)

@JsonClass(generateAdapter = true)
data class StartItineraryResponse(
    @Json(name = "message") val message: String,
    @Json(name = "scheduleItId") val scheduleItId: String
)

@JsonClass(generateAdapter = true)
data class EndItineraryRequest(
    @Json(name = "endTime") val endTime: String // ISO-8601
)

@JsonClass(generateAdapter = true)
data class SimpleMessage(
    @Json(name = "message") val message: String
)
