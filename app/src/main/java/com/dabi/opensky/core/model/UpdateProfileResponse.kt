package com.dabi.opensky.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class UpdateProfileResponse(
    @Json(name = "message")
    val message: String,
    @Json(name = "profile")
    val profile: Profile
)

@JsonClass(generateAdapter = true)
data class Profile(
    @Json(name = "userID")
    val userID: String?,
    @Json(name = "email")
    val email: String?,
    @Json(name = "fullName")
    val fullName: String?,
    @Json(name = "role")
    val role: String?,
    @Json(name = "phoneNumber")
    val phoneNumber: String? = null,
    @Json(name = "citizenId")
    val citizenId: String? = null,
    @Json(name = "dob")
    val dob: String? = null, // yyyy-MM-dd
    @Json(name = "avatarURL")
    val avatarURL: String? = null,
    @Json(name = "createdAt")
    val createdAt: String?
)

