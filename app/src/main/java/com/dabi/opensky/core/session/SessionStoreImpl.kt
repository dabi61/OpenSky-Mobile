package com.dabi.opensky.core.session

import android.os.Build
import androidx.annotation.RequiresApi
import com.dabi.opensky.core.data.remote.model.response.AuthResponse
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
    private val _state = MutableStateFlow(AuthResponse(null, null, null, null))
    override val state: StateFlow<AuthResponse> = _state

    init {
        // nạp từ DataStore vào cache khi app start
        appScope.launch(Dispatchers.IO) {
            _state.value = ds.read()
        }
    }

    override fun getAccess() = _state.value.access
    override fun getRefresh() = _state.value.refresh

    @RequiresApi(Build.VERSION_CODES.O)
    override fun isAccessExpiringSoon(cushionSec: Long): Boolean {
        val exp = _state.value.accessExpSec ?: return false
        val now = Instant.now().epochSecond
        return exp - now <= cushionSec
    }

    override suspend fun update(newTokens: AuthResponse?) = mutex.withLock {
        val t = newTokens ?: AuthResponse(null, null, null, null)
        ds.write(t)
        _state.value = t
    }

    override suspend fun clear() = update(null)
}