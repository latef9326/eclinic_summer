package com.example.eclinic_summer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EClinicApplication : Application() {  // Zmieniona nazwa klasy
    override fun onCreate() {
        super.onCreate()
        // Inicjalizacja Firebase itp.
    }
}