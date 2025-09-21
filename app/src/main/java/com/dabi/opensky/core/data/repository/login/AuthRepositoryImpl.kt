package com.dabi.opensky.core.data.repository.login

import android.os.Build
import androidx.annotation.RequiresApi
import com.dabi.opensky.core.data.remote.api.LoginService
import com.dabi.opensky.core.data.remote.model.request.AuthRequest
import com.dabi.opensky.core.data.remote.model.response.AuthResponse
import com.dabi.opensky.core.data.remote.model.response.toEpochSec
import com.dabi.opensky.core.session.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val loginService: LoginService,
    private val session: SessionStore
) : AuthRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun login(username: String, password: String): Result<Unit> {
        return try {
            val res = loginService.login(AuthRequest(username, password))
            if (!res.isSuccessful) {
                return Result.failure(Exception("Login failed: ${res.code()}"))
            }
            val body = res.body() ?: return Result.failure(Exception("Empty body"))

            session.update(
                AuthResponse(
                    access = body.accessToken,
                    refresh = body.refreshToken,
                    accessExpSec = body.accessTokenExpires.toEpochSec(),
                    refreshExpSec = body.refreshTokenExpires.toEpochSec()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        // (tuỳ chọn) gọi /auth/revoke refreshToken ở đây trước
        session.clear()
    }

    override val isLoggedIn: Flow<Boolean> =
        session.state.map { !it.access.isNullOrEmpty() && !it.refresh.isNullOrEmpty() }
}