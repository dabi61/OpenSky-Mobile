package com.dabi.opensky.core.data.remote.interceptor

import com.dabi.opensky.core.designsystem.Constant
import com.dabi.opensky.core.session.SessionStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor which adds authorization token in header.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val session: SessionStore,
    private val baseHost: String,
) : Interceptor {

    private val skipPaths = setOf("/auth/login", "/auth/refresh")

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        if (req.url.host != baseHost || req.url.encodedPath in skipPaths) {
            return chain.proceed(req)
        }

        val access = session.getAccess()
        if (access.isNullOrEmpty()) return chain.proceed(req)

        val newReq = req.newBuilder()
            .header("Authorization", "Bearer $access")
            .build()
        return chain.proceed(newReq)
    }
}
