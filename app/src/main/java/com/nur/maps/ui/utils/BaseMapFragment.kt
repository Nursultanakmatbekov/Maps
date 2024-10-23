package com.nur.maps.ui.utils

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import javax.inject.Inject

abstract class BaseMapFragment : Fragment() {

    @Inject
    lateinit var mapInterface: MapInterface

    abstract fun getMapContainer(): View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapInterface.initializeMap(getMapContainer(), requireContext(), savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        mapInterface.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapInterface.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapInterface.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapInterface.onLowMemory()
    }
}
