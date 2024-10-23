package com.nur.maps.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.nur.maps.R
import com.nur.maps.databinding.FragmentMapDisplayBinding
import com.nur.maps.ui.utils.BaseMapFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MapDisplayFragment @Inject constructor() : BaseMapFragment() {

    private val binding by viewBinding(FragmentMapDisplayBinding::bind)

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                checkLocationSettings()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Разрешение на доступ к местоположению отклонено",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        locationSettingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                showCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Геолокация отключена", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_map_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupSearchListener()
    }

    private fun setupClickListeners() {
        binding.btnShowCurrentLocation.setOnClickListener {
            checkLocationSettings()
        }
    }

    private fun checkLocationSettings() {
        mapInterface.checkLocationSettings(
            requireContext(),
            requestPermissionLauncher,
            locationSettingsLauncher
        ) {
            showCurrentLocation()
        }
    }

    private fun showCurrentLocation() {
        mapInterface.getCurrentLocation(requireContext()) { latitude, longitude ->
            mapInterface.moveCamera(latitude, longitude, 15f)
            mapInterface.addMarker(latitude, longitude, "Ваше местоположение")
        }
    }

    private fun setupSearchListener() {
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                mapInterface.search(query, requireContext())
            }
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    mapInterface.removeMarker()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun getMapContainer(): View {
        return binding.mapContainer
    }
}
