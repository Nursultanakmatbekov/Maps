package com.nur.maps.ui.di

import com.nur.maps.ui.utils.GoogleMapHandler
import com.nur.maps.ui.utils.MapInterface
import com.nur.maps.ui.utils.YandexMapHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {

    @Binds
    abstract fun bindMapHandler(handler: YandexMapHandler): MapInterface
}
