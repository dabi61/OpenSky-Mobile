package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.model.UpdateProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part

interface ProfileService {
    /**
     * Multipart form-data upload profile
     * Example endpoint; adjust path to your backend
     */
    @Multipart
    @PUT("users/profile")
    suspend fun updateProfile(
        @Part("fullName") fullName: RequestBody?,
        @Part("phoneNumber") phoneNumber: RequestBody?,
        @Part("citizenId") citizenId: RequestBody?,
        @Part("dob") dob: RequestBody?, // yyyy-MM-dd
        @Part avatar: MultipartBody.Part? // "avatar" file part
    ): Response<UpdateProfileResponse>
}
