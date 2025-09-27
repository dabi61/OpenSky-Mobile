package com.dabi.opensky.core.data.repository

import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.api.ProfileService
import com.dabi.opensky.core.model.UpdateProfileResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val api: ProfileService
) {
    suspend fun updateProfile(
        fullName: String?,
        phoneNumber: String?,
        citizenId: String?,
        dob: String?,
        avatarFile: File?
    ): Resource<UpdateProfileResponse> = try {
        val text = "text/plain".toMediaType()
        val nameBody = fullName?.toRequestBody(text)
        val phoneBody = phoneNumber?.toRequestBody(text)
        val citizenBody = citizenId?.toRequestBody(text)
        val dobBody = dob?.toRequestBody(text)

        val avatarPart = avatarFile?.let { f ->
            val mime = when {
                f.name.endsWith(".png", true) -> "image/png"
                f.name.endsWith(".webp", true) -> "image/webp"
                else -> "image/jpeg"
            }.toMediaTypeOrNull()
            val rb = f.asRequestBody(mime)
            MultipartBody.Part.createFormData("avatar", f.name, rb)
        }
        val res = api.updateProfile(nameBody, phoneBody, citizenBody, dobBody, avatarPart)
        if (res.isSuccessful) Resource.Success(requireNotNull(res.body()))
        else Resource.Error(IllegalStateException("HTTP ${res.code()}"))
    } catch (t: Throwable) {
        Resource.Error(t)
    }
}