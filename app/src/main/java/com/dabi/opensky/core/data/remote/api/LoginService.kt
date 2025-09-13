package com.dabi.opensky.core.data.remote.api

import com.dabi.opensky.core.data.remote.model.request.AuthRequest
import com.dabi.opensky.core.data.remote.model.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("auth/login")
    suspend fun login(@Body body: AuthRequest): Response<LoginResponse>
}