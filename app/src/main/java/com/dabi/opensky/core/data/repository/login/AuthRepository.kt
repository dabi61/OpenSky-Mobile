package com.dabi.opensky.core.data.repository.login

import com.dabi.opensky.core.session.SessionStore
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Login: lưu token nếu thành công */
    suspend fun login(username: String, password: String): Result<Unit>

    /** Logout: xoá session (và gọi revoke nếu cần) */
    suspend fun logout()

    /** Trạng thái đăng nhập (UI quan sát để điều hướng) */
    val isLoggedIn: Flow<Boolean>
}