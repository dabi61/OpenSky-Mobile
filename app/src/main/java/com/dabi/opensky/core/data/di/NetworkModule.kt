package com.dabi.opensky.core.data.di

import android.content.Context
import com.dabi.opensky.core.auth.TokenAuthenticator
import com.dabi.opensky.core.data.remote.api.AuthApi
import com.dabi.opensky.core.data.remote.api.HotelService
import com.dabi.opensky.core.data.remote.api.LoginService
import com.dabi.opensky.core.event.AppEventManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.dabi.opensky.core.data.remote.interceptor.AuthInterceptor
import com.dabi.opensky.core.session.SessionStore
import com.dabi.opensky.core.session.SessionStoreImpl
import com.dabi.opensky.core.session.TokenDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

private const val BASE_URL = "https://opensky-be-production.up.railway.app/" // TODO đổi theo BE

@Qualifier
annotation class BaseApiHost

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideTokenDataSource(@ApplicationContext ctx: Context) = TokenDataSource(ctx)

    @Provides @Singleton
    fun provideSessionStore(ds: TokenDataSource): SessionStore =
        SessionStoreImpl(ds, appScope = CoroutineScope(Dispatchers.IO))

    @Provides @Singleton @BaseApiHost
    fun provideBaseHost(): String = BASE_URL.toHttpUrl().host

    @Provides @Singleton
    fun provideOkHttp(
        session: SessionStore,
        @BaseApiHost baseHost: String,
        eventManager: AppEventManager,
        authApiLazy: dagger.Lazy<AuthApi> // để tránh vòng phụ thuộc
    ): OkHttpClient {
        val authInterceptor = AuthInterceptor(session, baseHost)
        // TokenAuthenticator cần AuthApi, mà AuthApi cần Retrofit (cần OkHttp).
        // Dùng Lazy để chỉ tạo AuthApi sau khi Retrofit có sẵn.
        val clientBuilder = OkHttpClient.Builder()
        val client = clientBuilder.build() // tạm build để tạo retrofit dưới

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val authApi = retrofit.create(AuthApi::class.java)
        val authenticator = TokenAuthenticator(session, authApi, eventManager)

        return client.newBuilder()
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideLoginService(retrofit: Retrofit): LoginService = retrofit.create(LoginService::class.java)

    @Provides @Singleton
    fun provideHotelApi(retrofit: Retrofit): HotelService = retrofit.create(HotelService::class.java)
}