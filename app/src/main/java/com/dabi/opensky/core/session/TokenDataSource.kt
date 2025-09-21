package com.dabi.opensky.core.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dabi.opensky.core.data.remote.model.response.AuthResponse
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
    }

    suspend fun read(): AuthResponse {
        val p = ctx.dataStore.data.first()
        return AuthResponse(
            p[K.ACCESS],
            p[K.REFRESH],
            p[K.ACCESS_EXP],
            p[K.REFRESH_EXP]
        )
    }

    suspend fun write(t: AuthResponse) {
        ctx.dataStore.edit { e ->
            e[K.ACCESS] = t.access ?: ""
            e[K.REFRESH] = t.refresh ?: ""
            t.accessExpSec?.let { e[K.ACCESS_EXP] = it } ?: e.remove(K.ACCESS_EXP)
            t.refreshExpSec?.let { e[K.REFRESH_EXP] = it } ?: e.remove(K.REFRESH_EXP)
        }
    }

    suspend fun clear() { ctx.dataStore.edit { it.clear() } }
}