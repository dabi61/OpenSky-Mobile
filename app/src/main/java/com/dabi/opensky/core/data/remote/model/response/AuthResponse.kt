
package com.dabi.opensky.core.data.remote.model.response

import android.os.Build
import androidx.annotation.RequiresApi
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class AuthResponse(
    val access: String?,
    val refresh: String?,
    val accessExpSec: Long?,   // epoch seconds (UTC)
    val refreshExpSec: Long?,
)

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
    @Json(name = "status")
    val status: String,
    @Json(name = "phoneNumber")
    val phoneNumber: String?,
    @Json(name = "citizenId")
    val citizenId: String?,
    @Json(name = "doB")
    val doB: String?,
    @Json(name = "avatarURL")
    val avatarURL: String?,
    @Json(name = "createdAt")
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "accessToken")
    val accessToken: String,
    @Json(name = "refreshToken")
    val refreshToken: String,
    @Json(name = "accessTokenExpires")
    val accessTokenExpires: String,   // ví dụ: 2025-09-13T10:56:41.3667629Z
    @Json(name = "refreshTokenExpires")
    val refreshTokenExpires: String,
    @Json(name = "user")
    val user: User
)

@JsonClass(generateAdapter = true)
data class RefreshResponse(
    @Json(name = "accessToken")
    val accessToken: String,
    @Json(name = "accessTokenExpires")
    val accessTokenExpires: String
)

@RequiresApi(Build.VERSION_CODES.O)
fun String.toEpochSec(): Long = java.time.Instant.parse(this).epochSecond