package com.dabi.opensky.core.auth


import android.os.Build
import androidx.annotation.RequiresApi
import com.dabi.opensky.core.data.remote.api.AuthApi
import com.dabi.opensky.core.data.remote.api.RefreshReq
import com.dabi.opensky.core.data.remote.api.RefreshRes
import com.dabi.opensky.core.data.remote.model.response.AuthResponse
import com.dabi.opensky.core.data.remote.model.response.toEpochSec
import com.dabi.opensky.core.event.AppEvent
import com.dabi.opensky.core.event.AppEventManager
import com.dabi.opensky.core.session.SessionStore
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.min
import kotlinx.coroutines.runBlocking

class TokenAuthenticator(
    private val session: SessionStore,
    private val authApi: AuthApi,
    private val eventManager: AppEventManager
) : Authenticator {

    private val lock = ReentrantLock()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun authenticate(route: Route?, response: Response): Request? {
        // tránh vòng lặp 401 vô hạn
        if (responseCount(response) >= 2) return null

        val reqAccess = response.request.header("Authorization")?.removePrefix("Bearer")?.trim()
        val curAccess = session.getAccess()
        if (!curAccess.isNullOrEmpty() && curAccess != reqAccess) {
            // thread khác vừa refresh xong
            return response.request.newBuilder()
                .header("Authorization", "Bearer $curAccess")
                .build()
        }

        lock.lock()
        try {
            // double-check sau khi khoá
            val latest = session.getAccess()
            if (!latest.isNullOrEmpty() && latest != reqAccess) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latest")
                    .build()
            }

            val refresh = session.getRefresh() ?: return null

            val refreshed = refreshWithBackoff(refresh) ?: run {
                // refresh hết hạn/invalid → xoá session → emit event → UI logout
                runBlocking { session.clear() }
                eventManager.emitEvent(AppEvent.TokenExpired)
                return null
            }

            val accessExp = refreshed.accessTokenExpires.toEpochSec()
            runBlocking {
                session.update(
                    AuthResponse(
                        access = refreshed.accessToken,
                        refresh = refresh, // BE không trả refresh mới
                        accessExpSec = accessExp,
                        refreshExpSec = session.state.value.refreshExpSec
                    )
                )
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshed.accessToken}")
                .build()

        } finally { lock.unlock() }
    }

    private fun refreshWithBackoff(refresh: String): RefreshRes? {
        var delay = 1000L
        repeat(3) {
            try {
                val res = authApi.refresh(RefreshReq(refresh)).execute()
                if (res.isSuccessful) return res.body()

                when (res.code()) {
                    400, 401, 403 -> return null // dừng hẳn
                    else -> { /* 429/5xx -> retry */ }
                }
            } catch (_: IOException) { /* mạng lỗi -> retry */ }

            Thread.sleep(delay + (0..250).random())
            delay = min(delay * 2, 8000L)
        }
        return null
    }

    private fun responseCount(r: Response): Int {
        var c = 1; var cur = r
        while (cur.priorResponse != null) { c++; cur = cur.priorResponse!! }
        return c
    }
}
