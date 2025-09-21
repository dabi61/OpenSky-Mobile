/*
 * Copyright 2020 Shreyas Patil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dabi.opensky.core.data.remote.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class RefreshReq(
    @Json(name = "refreshToken")
    val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class RefreshRes(
    @Json(name = "accessToken")
    val accessToken: String,
    @Json(name = "accessTokenExpires")
    val accessTokenExpires: String
)

interface AuthApi {
    @POST("auth/refresh")
    fun refresh(@Body body: RefreshReq): Call<RefreshRes>
}