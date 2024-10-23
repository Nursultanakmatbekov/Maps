package com.nur.maps

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App:Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("25e1223c-f185-4f4e-836b-68ca1b907586")
    }
}