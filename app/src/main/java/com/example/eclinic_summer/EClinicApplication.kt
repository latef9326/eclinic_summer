package com.example.eclinic_summer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class EClinicApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicjalizacja Timber tylko w wersji debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}