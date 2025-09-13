package com.dabi.opensky

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class OpenSkyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Cấu hình logging
        initializeLogging()

        // Cấu hình crash reporting
        initializeCrashReporting()

        // Cấu hình analytics
        initializeAnalytics()

        // Cấu hình ngôn ngữ
        initializeLocalization()
    }

    private fun initializeLogging() {
//        if (BuildConfig.DEBUG) {
//            Timber.plant(Timber.DebugTree())
//        }
    }

    private fun initializeCrashReporting() {
        // Firebase Crashlytics, Bugsnag, etc.
    }

    private fun initializeAnalytics() {
        // Firebase Analytics, custom tracking
    }

    private fun initializeLocalization() {
        // Thiết lập ngôn ngữ mặc định nếu cần
    }
}