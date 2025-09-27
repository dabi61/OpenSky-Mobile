package com.dabi.opensky.core.session

import android.os.Build
import androidx.annotation.RequiresApi
import com.dabi.opensky.core.data.remote.model.response.AuthResponse
import com.dabi.opensky.core.data.remote.model.response.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

class SessionStoreImpl(
    private val ds: TokenDataSource,
    appScope: CoroutineScope
) : SessionStore {

    private val mutex = Mutex()
    private val _state = MutableStateFlow(AuthResponse()) // 5 tham số default = null
    override val state: StateFlow<AuthResponse> = _state.asStateFlow()

    init {
        appScope.launch(Dispatchers.IO) {
            _state.value = ds.read()
        }
    }

    override fun getAccess() = _state.value.access
    override fun getRefresh() = _state.value.refresh

    @RequiresApi(Build.VERSION_CODES.O)
    override fun isAccessExpiringSoon(cushionSec: Long): Boolean {
        val exp = _state.value.accessExpSec ?: return false
        val now = java.time.Instant.now().epochSecond
        return exp - now <= cushionSec
    }

    override suspend fun update(newTokens: AuthResponse?) = mutex.withLock {
        val t = newTokens ?: AuthResponse()
        ds.write(t)
        _state.value = t
    }

    override suspend fun clear() = update(null)

    // ✅ cái bạn cần: emit ngay + lưu DataStore
    override suspend fun setUser(user: User?) = mutex.withLock {
        val current = _state.value
        val next = current.copy(user = user)
        ds.write(next)
        _state.value = next                   // ✅ emit ngay để UI recompose
    }
}