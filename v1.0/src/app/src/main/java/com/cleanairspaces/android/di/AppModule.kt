package com.cleanairspaces.android.di

import com.cleanairspaces.android.models.repository.LocationsRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideLocationsRepo(): LocationsRepo = LocationsRepo()
}