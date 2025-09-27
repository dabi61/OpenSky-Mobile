package com.dabi.opensky.core.session

import com.dabi.opensky.core.data.remote.model.response.AuthResponse
import com.dabi.opensky.core.data.remote.model.response.User
import kotlinx.coroutines.flow.StateFlow



interface SessionStore {
    val state: StateFlow<AuthResponse>
    fun getAccess(): String?
    fun getRefresh(): String?
    fun isAccessExpiringSoon(cushionSec: Long = 90): Boolean
    suspend fun update(newTokens: AuthResponse?)
    suspend fun clear()
    suspend fun setUser(user: User?)
}