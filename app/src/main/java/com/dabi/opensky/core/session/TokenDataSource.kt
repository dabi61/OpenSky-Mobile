package com.dabi.opensky.core.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dabi.opensky.core.data.remote.model.response.AuthResponse
import com.dabi.opensky.core.data.remote.model.response.User
import com.dabi.opensky.core.utils.moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenDataSource @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val Context.dataStore by preferencesDataStore(
        name = "auth.ds"
    )

    private object K {
        val ACCESS = stringPreferencesKey("access")
        val REFRESH = stringPreferencesKey("refresh")
        val ACCESS_EXP = longPreferencesKey("access_exp")
        val REFRESH_EXP = longPreferencesKey("refresh_exp")
        val USER = stringPreferencesKey("user")
    }

    private val userAdapter = moshi.adapter(User::class.java)

    suspend fun read(): AuthResponse {
        val p = ctx.dataStore.data.first()
        val access = p[K.ACCESS].orEmpty().ifBlank { null }
        val refresh = p[K.REFRESH].orEmpty().ifBlank { null }
        val accessExp = p[K.ACCESS_EXP]
        val refreshExp = p[K.REFRESH_EXP]
        val userJson = p[K.USER].orEmpty().ifBlank { null }
        val user = userJson?.let { runCatching { userAdapter.fromJson(it) }.getOrNull() }

        return AuthResponse(
            access = access,
            refresh = refresh,
            accessExpSec = accessExp,
            refreshExpSec = refreshExp,
            user = user
        )
    }

    suspend fun write(t: AuthResponse) {
        ctx.dataStore.edit { e ->
            e[K.ACCESS] = t.access ?: ""
            e[K.REFRESH] = t.refresh ?: ""
            if (t.accessExpSec != null) e[K.ACCESS_EXP] = t.accessExpSec else e.remove(K.ACCESS_EXP)
            if (t.refreshExpSec != null) e[K.REFRESH_EXP] = t.refreshExpSec else e.remove(K.REFRESH_EXP)

            val userJson = t.user?.let { userAdapter.toJson(it) } ?: ""
            e[K.USER] = userJson
        }
    }

    suspend fun clear() { ctx.dataStore.edit { it.clear() } }
}