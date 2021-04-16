package com.cleanairspaces.android.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.cleanairspaces.android.models.CasDatabase
import com.cleanairspaces.android.models.api.InOutDoorLocationsApiService
import com.cleanairspaces.android.models.api.QrScannedItemsApiService
import com.cleanairspaces.android.models.dao.*
import com.cleanairspaces.android.models.repository.OutDoorLocationsRepo
import com.cleanairspaces.android.models.repository.ScannedDevicesRepo
import com.cleanairspaces.android.utils.BASE_URL
import com.cleanairspaces.android.utils.DATABASE_NAME
import com.cleanairspaces.android.utils.DataStoreManager
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
    fun provideOutDoorLocationsApiService(retrofit: Retrofit): InOutDoorLocationsApiService =
            retrofit.create(InOutDoorLocationsApiService::class.java)

    @Provides
    @Singleton
    fun provideQrScannedItemsApiService(retrofit: Retrofit): QrScannedItemsApiService =
            retrofit.create(QrScannedItemsApiService::class.java)

    @Provides
    @Singleton
    fun provideCoroutineScopeIO(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    @Provides
    @Singleton
    fun provideDatabase(app: Application): CasDatabase =
            Room.databaseBuilder(app, CasDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()

    @Provides
    @Singleton
    fun provideWorkManager(app: Application): WorkManager =
            WorkManager.getInstance(app.applicationContext)

    @Provides
    @Singleton
    fun provideOutDoorLocationsDao(casDatabase: CasDatabase): OutDoorLocationsDao =
            casDatabase.outDoorLocationsDao()


    @Provides
    @Singleton
    fun provideCustomerDeviceDataDao(casDatabase: CasDatabase): LocDataFromQrDao =
            casDatabase.customerDeviceDataDao()

    @Provides
    @Singleton
    fun provideMyLocationDetailsDao(casDatabase: CasDatabase): LocationDetailsDao =
            casDatabase.myLocationDetailsDao()

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
    fun provideSearchSuggestionsDao(casDatabase: CasDatabase): SearchSuggestionsDao =
        casDatabase.searchSuggestionsDao()

    @Provides
    @Singleton
    fun provideLocationsRepo(
        inOutDoorLocationsApiService: InOutDoorLocationsApiService,
        coroutineScope: CoroutineScope,
        outDoorLocationsDao: OutDoorLocationsDao,
        searchSuggestionsDao: SearchSuggestionsDao
    ): OutDoorLocationsRepo = OutDoorLocationsRepo(
            inOutDoorLocationsApiService = inOutDoorLocationsApiService,
            coroutineScope = coroutineScope,
            outDoorLocationsDao = outDoorLocationsDao,
        searchSuggestionsDao=searchSuggestionsDao
    )


    @Provides
    @Singleton
    fun provideScannedDevicesRepo(
            qrScannedItemsApiService: QrScannedItemsApiService,
            coroutineScope: CoroutineScope,
            locDataFromQrDao: LocDataFromQrDao,
            locationDetailsDao: LocationDetailsDao,
            locationHistoryThreeDaysDao: LocationHistoryThreeDaysDao,
            locationHistoryWeekDao: LocationHistoryWeekDao,
            locationHistoryMonthDao: LocationHistoryMonthDao,
            locationHistoryUpdatesTrackerDao: LocationHistoryUpdatesTrackerDao,
            searchSuggestionsDao: SearchSuggestionsDao
    ): ScannedDevicesRepo = ScannedDevicesRepo(
            qrScannedItemsApiService = qrScannedItemsApiService,
            coroutineScope = coroutineScope,
            locDataFromQrDao = locDataFromQrDao,
            locationDetailsDao = locationDetailsDao,
            locationHistoryThreeDaysDao = locationHistoryThreeDaysDao,
            locationHistoryWeekDao = locationHistoryWeekDao,
            locationHistoryMonthDao = locationHistoryMonthDao,
            locationHistoryUpdatesTrackerDao = locationHistoryUpdatesTrackerDao,
        searchSuggestionsDao = searchSuggestionsDao
    )

    @Singleton
    @Provides
    fun provideDataStoreMgr(
            @ApplicationContext context: Context
    ): DataStoreManager {
        return DataStoreManager(context)
    }
}