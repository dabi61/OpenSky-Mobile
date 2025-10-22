package com.dabi.opensky.core.data.repository.login

import com.dabi.opensky.core.data.remote.model.response.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Login: lưu token nếu thành công */
    suspend fun login(username: String, password: String): Result<Unit>

    /** Register: tạo tài khoản mới (API không trả token) */
    suspend fun register(fullName: String, email: String, password: String): Result<User>

    /** Logout: xoá session (và gọi revoke nếu cần) */
    suspend fun logout()

    /** Trạng thái đăng nhập (UI quan sát để điều hướng) */
    val isLoggedIn: Flow<Boolean>
}
