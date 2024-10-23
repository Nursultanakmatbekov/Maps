package com.nur.maps.ui.utils

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

interface MapInterface {
    fun initializeMap(view: View, context: Context, savedInstanceState: Bundle?)
    fun moveCamera(latitude: Double, longitude: Double, zoomLevel: Float)
    fun addMarker(latitude: Double, longitude: Double, title: String)
    fun removeMarker()
    fun getCurrentLocation(context: Context, onLocationReceived: (latitude: Double, longitude: Double) -> Unit)
    fun checkLocationSettings(context: Context, requestPermissionLauncher: ActivityResultLauncher<String>, locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest>,        onLocationEnabled: () -> Unit)
    fun search(query: String, context: Context)
    fun onResume()
    fun onPause()
    fun onDestroy()
    fun onLowMemory()
}
