package com.dabi.opensky.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "userID")
    val userID: String,
    
    @Json(name = "email")
    val email: String,
    
    @Json(name = "fullName")
    val fullName: String,
    
    @Json(name = "role")
    val role: String,
    
    @Json(name = "phoneNumber")
    val phoneNumber: String? = null,
    
    @Json(name = "citizenId")
    val citizenId: String? = null,
    
    @Json(name = "dob")
    val dob: String? = null, // Format: "2025-09-21"
    
    @Json(name = "avatarURL")
    val avatarURL: String? = null,
    
    @Json(name = "status")
    val status: String,
    
    @Json(name = "createdAt")
    val createdAt: String // Format: "2025-09-21T09:46:08.779Z"
)
