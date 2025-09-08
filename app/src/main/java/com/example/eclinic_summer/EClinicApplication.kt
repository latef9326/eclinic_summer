package com.example.eclinic_summer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class EClinicApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logging only in debug builds
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
