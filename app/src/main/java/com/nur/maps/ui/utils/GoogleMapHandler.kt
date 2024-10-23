package com.nur.maps.ui.utils

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale
import javax.inject.Inject

class GoogleMapHandler @Inject constructor() : MapInterface, OnMapReadyCallback {

    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null

    override fun initializeMap(view: View, context: Context, savedInstanceState: Bundle?) {
        if (view is FrameLayout) {
            mapView = MapView(context)
            view.addView(mapView)

            mapView?.onCreate(savedInstanceState)
            mapView?.getMapAsync(this)
        } else {
            throw IllegalArgumentException("View must be a FrameLayout")
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(
                mapView!!.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    override fun moveCamera(latitude: Double, longitude: Double, zoomLevel: Float) {
        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(latitude, longitude),
                zoomLevel
            )
        )
    }

    override fun addMarker(latitude: Double, longitude: Double, title: String) {
        currentMarker?.remove()
        currentMarker =
            googleMap?.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title(title))
    }

    override fun removeMarker() {
        currentMarker?.remove()
        currentMarker = null
    }

    override fun getCurrentLocation(
        context: Context,
        onLocationReceived: (latitude: Double, longitude: Double) -> Unit
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onLocationReceived(location.latitude, location.longitude)
                } else {
                    Log.e("GoogleMapHandler", "Не удалось получить местоположение")
                    Toast.makeText(
                        context,
                        "Не удалось получить местоположение",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(context, "Нет разрешения на доступ к местоположению", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun checkLocationSettings(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<String>,
        locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest>,
        onLocationEnabled: () -> Unit
    ) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).apply {
            setMinUpdateIntervalMillis(5000)
            setMinUpdateDistanceMeters(10f)
        }.build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(context)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            onLocationEnabled()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    locationSettingsLauncher.launch(
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(
                        context,
                        "Не удалось запустить настройки местоположения",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(context, "Настройки местоположения недоступны", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun search(query: String, context: Context) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                val latitude = location.latitude
                val longitude = location.longitude
                moveCamera(latitude, longitude, 15f)
                addMarker(latitude, longitude, query)
            } else {
                Toast.makeText(
                    context,
                    "Не удалось найти местоположение для запроса: \"$query\"",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("GoogleMapHandler", "Geocoder failed: ${e.message}")
        }
    }

    override fun onResume() {
        mapView?.onResume()
    }

    override fun onPause() {
        mapView?.onPause()
    }

    override fun onDestroy() {
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        mapView?.onLowMemory()
    }
}
