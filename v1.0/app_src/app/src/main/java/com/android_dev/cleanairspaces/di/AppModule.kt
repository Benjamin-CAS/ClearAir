package com.android_dev.cleanairspaces.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.android_dev.cleanairspaces.persistence.local.CasDatabase
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.persistence.local.models.dao.*
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.BASE_URL
import com.android_dev.cleanairspaces.utils.DATABASE_NAME
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    /* DEBUG ONLY
      for provideRetrofit() when building retrofit .client(getLogger())
     */
    private fun getLogger(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder().addInterceptor(interceptor).build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getLogger())
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                )
            ).build()


    @Provides
    @Singleton
    fun provideCoroutineScopeIO(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    @Provides
    @Singleton
    fun provideWorkManager(app: Application): WorkManager =
        WorkManager.getInstance(app.applicationContext)


    @Singleton
    @Provides
    fun provideDataStoreMgr(
        @ApplicationContext context: Context
    ): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideAppDataRepo(
        coroutineScope: CoroutineScope,
        mapDataDao: MapDataDao,
        searchSuggestionsDataDao: SearchSuggestionsDataDao,
        watchedLocationHighLightsDao: WatchedLocationHighLightsDao,
        locationHistoryThreeDaysDao: LocationHistoryThreeDaysDao,
        locationHistoryWeekDao: LocationHistoryWeekDao,
        locationHistoryMonthDao: LocationHistoryMonthDao,
        locationHistoryUpdatesTrackerDao: LocationHistoryUpdatesTrackerDao,
    ): AppDataRepo = AppDataRepo(
        coroutineScope = coroutineScope,
        mapDataDao = mapDataDao,
        searchSuggestionsDataDao = searchSuggestionsDataDao,
        watchedLocationHighLightsDao = watchedLocationHighLightsDao,
            locationHistoryThreeDaysDao = locationHistoryThreeDaysDao,
            locationHistoryWeekDao = locationHistoryWeekDao,
            locationHistoryMonthDao = locationHistoryMonthDao,
            locationHistoryUpdatesTrackerDao = locationHistoryUpdatesTrackerDao,
    )


    @Provides
    @Singleton
    fun provideMapDataDao(
        casDatabase: CasDatabase
    ): MapDataDao = casDatabase.mapDataDao()


    @Provides
    @Singleton
    fun provideSearchSuggestionsDataDao(
        casDatabase: CasDatabase
    ): SearchSuggestionsDataDao = casDatabase.searchSuggestionsDataDao()

    @Provides
    @Singleton
    fun provideWatchedLocationHighLightsDao(
        casDatabase: CasDatabase
    ): WatchedLocationHighLightsDao = casDatabase.watchedLocationHighLightsDao()

    @Provides
    @Singleton
    fun provideMyLocationHistoryThreeDaysDao(casDatabase: CasDatabase): LocationHistoryThreeDaysDao =
            casDatabase.locationHistoryThreeDaysDao()

    @Provides
    @Singleton
    fun provideLocationHistoryWeekDao(casDatabase: CasDatabase): LocationHistoryWeekDao =
            casDatabase.locationHistoryWeekDao()

    @Provides
    @Singleton
    fun provideLocationHistoryMonthDao(casDatabase: CasDatabase): LocationHistoryMonthDao =
            casDatabase.locationHistoryMonthDao()

    @Provides
    @Singleton
    fun provideLocationHistoryUpdatesTrackerDao(casDatabase: CasDatabase): LocationHistoryUpdatesTrackerDao =
            casDatabase.locationHistoryUpdatesTrackerDao()



    @Provides
    @Singleton
    fun provideDatabase(app: Application): CasDatabase =
        Room.databaseBuilder(app, CasDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
}