package com.nur.maps.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nur.maps.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}