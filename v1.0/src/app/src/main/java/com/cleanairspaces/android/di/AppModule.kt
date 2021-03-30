package com.cleanairspaces.android.di

import android.app.Application
import androidx.room.Room
import com.cleanairspaces.android.models.api.CasDatabase
import com.cleanairspaces.android.models.api.OutDoorLocationsApiService
import com.cleanairspaces.android.models.dao.OutDoorLocationsDao
import com.cleanairspaces.android.models.repository.OutDoorLocationsRepo
import com.cleanairspaces.android.utils.BASE_URL
import com.cleanairspaces.android.utils.DATABASE_NAME
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
            Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create(
                                    GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                            )
                    ).build()

    @Provides
    @Singleton
    fun provideOutDoorLocationsApiService(retrofit: Retrofit): OutDoorLocationsApiService =
            retrofit.create(OutDoorLocationsApiService::class.java)

    @Provides
    @Singleton
    fun provideCoroutineScopeIO() : CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    @Provides
    @Singleton
    fun provideDatabase(app: Application): CasDatabase =
            Room.databaseBuilder(app, CasDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()

    @Provides
    @Singleton
    fun provideOutDoorLocationsDao(casDatabase: CasDatabase): OutDoorLocationsDao =
            casDatabase.outDoorLocationsDao()

    @Provides
    @Singleton
    fun provideLocationsRepo(outDoorLocationsApiService: OutDoorLocationsApiService, coroutineScope: CoroutineScope, outDoorLocationsDao : OutDoorLocationsDao): OutDoorLocationsRepo = OutDoorLocationsRepo(outDoorLocationsApiService, coroutineScope, outDoorLocationsDao)
}