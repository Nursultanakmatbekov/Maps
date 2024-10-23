package com.nur.maps.ui.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import java.io.IOException
import javax.inject.Inject

class YandexMapHandler @Inject constructor() : MapInterface {

    private var mapView: MapView? = null
    private var currentPlacemark: PlacemarkMapObject? = null

    override fun initializeMap(view: View, context: Context, savedInstanceState: Bundle?) {
        if (view is FrameLayout) {
            MapKitFactory.initialize(context)
            mapView = MapView(context)
            view.addView(mapView)
            Log.d("YandexMapHandler", "Map is initialized")
            moveCamera(42.8746, 74.5690, 12f)
            addMarker(42.8746, 74.5690, "Бишкек")
        } else {
            throw IllegalArgumentException("View must be a FrameLayout")
        }
    }

    override fun moveCamera(latitude: Double, longitude: Double, zoomLevel: Float) {
        mapView?.mapWindow?.map?.move(
            CameraPosition(Point(latitude, longitude), zoomLevel, 0.0f, 0.0f)
        )
    }

    override fun addMarker(latitude: Double, longitude: Double, title: String) {
        currentPlacemark?.let {
            try {
                mapView?.mapWindow?.map?.mapObjects?.remove(it)
            } catch (e: Exception) {
                Log.e("YandexMapHandler", "Failed to remove existing placemark: ${e.message}")
            }
        }

        currentPlacemark = mapView?.mapWindow?.map?.mapObjects?.addPlacemark(Point(latitude, longitude))
        currentPlacemark?.setText(title)
    }

    override fun removeMarker() {
        currentPlacemark?.let {
            try {
                mapView?.mapWindow?.map?.mapObjects?.remove(it)
                currentPlacemark = null
            } catch (e: Exception) {
                Log.e("YandexMapHandler", "Error removing placemark: ${e.message}")
            }
        }
    }

    override fun getCurrentLocation(
        context: Context,
        onLocationReceived: (latitude: Double, longitude: Double) -> Unit
    ) {
        val locationManager = MapKitFactory.getInstance().createLocationManager()

        locationManager.requestSingleUpdate(object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                onLocationReceived(location.position.latitude, location.position.longitude)
                Log.d("YandexMapHandler", "lat=${location.position.latitude} lon=${location.position.longitude}")
            }

            override fun onLocationStatusUpdated(locationStatus: LocationStatus) {
                Log.d("YandexMapHandler", locationStatus.toString())
            }
        })
    }

    override fun checkLocationSettings(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<String>,
        locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest>,
        onLocationEnabled: () -> Unit
    ) {
        if (requestEnableLocationSettings(context)) {
            onLocationEnabled()
        } else {
            showLocationSettingsDialog(context)
        }
    }

    private fun requestEnableLocationSettings(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showLocationSettingsDialog(context: Context) {
        AlertDialog.Builder(context)
            .setMessage("Чтобы улучшить работы приложения, включите точность геолокации на устройстве")
            .setPositiveButton("Включить") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
            .setNegativeButton("Нет, спасибо", null)
            .show()
    }

    override fun search(query: String, context: Context) {
        val geocoder = Geocoder(context)
        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                moveCamera(address.latitude, address.longitude, 12f)
                addMarker(address.latitude, address.longitude, address.featureName ?: "Найдено место")
            } else {
                Log.d("YandexMapHandler", "No results found for query: $query")
            }
        } catch (e: IOException) {
            Log.e("YandexMapHandler", "Geocoder failed", e)
        }
    }



    override fun onResume() {
        MapKitFactory.getInstance().onStart()
        mapView?.onStart()
    }

    override fun onPause() {
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onDestroy() {
        mapView?.onStop()
    }

    override fun onLowMemory() {
        mapView?.onStop()
    }
}
