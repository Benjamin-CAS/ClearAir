package com.android_dev.cleanairspaces.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.android_dev.cleanairspaces.persistence.api.mqtt.CasMqttClient
import com.android_dev.cleanairspaces.persistence.api.services.*
import com.android_dev.cleanairspaces.persistence.local.CasDatabase
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.persistence.local.models.dao.*
import com.android_dev.cleanairspaces.repositories.api_facing.*
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.BASE_URL
import com.android_dev.cleanairspaces.utils.DATABASE_NAME
import com.android_dev.cleanairspaces.utils.MyLogger
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    /* IS_DEBUG_MODE ONLY
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
    fun provideOutDoorLocationService(retrofit: Retrofit): OutDoorLocationApiService =
        retrofit.create(OutDoorLocationApiService::class.java)

    @Provides
    @Singleton
    fun provideInDoorLocationService(retrofit: Retrofit): InDoorLocationApiService =
        retrofit.create(InDoorLocationApiService::class.java)

    @Provides
    @Singleton
    fun provideLocationDetailsService(retrofit: Retrofit): LocationDetailsService =
        retrofit.create(LocationDetailsService::class.java)

    @Provides
    @Singleton
    fun provideQrScannedItemsApiService(retrofit: Retrofit): QrScannedItemsApiService =
        retrofit.create(QrScannedItemsApiService::class.java)

    @Provides
    @Singleton
    fun provideLoggerService(retrofit: Retrofit): LoggerService =
        retrofit.create(LoggerService::class.java)

    @Provides
    @Singleton
    fun provideLocationHistoriesService(retrofit: Retrofit): LocationHistoriesService =
        retrofit.create(LocationHistoriesService::class.java)


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
        mapDataDao: MapDataDao,
        searchSuggestionsDataDao: SearchSuggestionsDataDao,
        watchedLocationHighLightsDao: WatchedLocationHighLightsDao,
        locationHistoryThreeDaysDao: LocationHistoryThreeDaysDao,
        locationHistoryWeekDao: LocationHistoryWeekDao,
        locationHistoryMonthDao: LocationHistoryMonthDao,
        locationHistoryUpdatesTrackerDao: LocationHistoryUpdatesTrackerDao,
        locationHistoriesService: LocationHistoriesService,
        inDoorLocationsApiService: InDoorLocationApiService,
        myLogger: MyLogger,
        monitorDetailsDataDao: MonitorDetailsDataDao,
        deviceDetailsDao: DeviceDetailsDao
    ): AppDataRepo = AppDataRepo(
        mapDataDao = mapDataDao,
        searchSuggestionsDataDao = searchSuggestionsDataDao,
        watchedLocationHighLightsDao = watchedLocationHighLightsDao,
        locationHistoryThreeDaysDao = locationHistoryThreeDaysDao,
        locationHistoryWeekDao = locationHistoryWeekDao,
        locationHistoryMonthDao = locationHistoryMonthDao,
        locationHistoryUpdatesTrackerDao = locationHistoryUpdatesTrackerDao,
        locationHistoriesService = locationHistoriesService,
        inDoorLocationsApiService = inDoorLocationsApiService,
        myLogger = myLogger,
        monitorDetailsDataDao = monitorDetailsDataDao,
        deviceDetailsDao = deviceDetailsDao
    )

    @Provides
    @Singleton
    fun provideMonitorDetailsUpdatesRepo(
        monitorDetailsDataDao: MonitorDetailsDataDao,
        inDoorLocationApiService: InDoorLocationApiService,
        myLogger: MyLogger
    ): MonitorDetailsUpdatesRepo = MonitorDetailsUpdatesRepo(
        monitorDetailsDataDao = monitorDetailsDataDao,
        inDoorLocationApiService = inDoorLocationApiService,
        myLogger = myLogger
    )

    @Provides
    @Singleton
    fun provideOutDoorLocationsRepo(
        mapDataDao: MapDataDao,
        searchSuggestionsDataDao: SearchSuggestionsDataDao,
        outDoorLocationApiService: OutDoorLocationApiService,
        myLogger: MyLogger
    ): OutDoorLocationsRepo = OutDoorLocationsRepo(
        mapDataDao = mapDataDao,
        searchSuggestionsDataDao = searchSuggestionsDataDao,
        outDoorLocationApiService = outDoorLocationApiService,
        myLogger = myLogger,
    )

    @Provides
    @Singleton
    fun provideLocationDetailsRepo(
        myLogger: MyLogger,
        qrScannedItemsApiService: QrScannedItemsApiService,

        ): LocationDetailsRepo = LocationDetailsRepo(
        myLogger = myLogger,
        qrScannedItemsApiService = qrScannedItemsApiService,

        )

    @Provides
    @Singleton
    fun provideInDoorLocationsRepo(
        searchSuggestionsDataDao: SearchSuggestionsDataDao,
        inDoorLocationApiService: InDoorLocationApiService,
        myLogger: MyLogger
    ): InDoorLocationsRepo = InDoorLocationsRepo(
        searchSuggestionsDataDao = searchSuggestionsDataDao,
        inDoorLocationsApiService = inDoorLocationApiService,
        myLogger = myLogger,
    )

    @Provides
    @Singleton
    fun provideWatchedLocationUpdatesRepo(
        locationDetailsService: LocationDetailsService,
        watchedLocationHighLightsDao: WatchedLocationHighLightsDao,
        myLogger: MyLogger
    ): WatchedLocationUpdatesRepo = WatchedLocationUpdatesRepo(
        watchedLocationHighLightsDao = watchedLocationHighLightsDao,
        locationDetailsService = locationDetailsService,
        myLogger = myLogger,
    )

    @Provides
    @Singleton
    fun provideMapDataDao(
        casDatabase: CasDatabase
    ): MapDataDao = casDatabase.mapDataDao()

    @Provides
    @Singleton
    fun provideMonitorDetailsDataDao(
        casDatabase: CasDatabase
    ): MonitorDetailsDataDao = casDatabase.monitorDetailsDataDao()

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
    fun provideLogsDao(casDatabase: CasDatabase): LogsDao =
        casDatabase.logsDao()

    @Provides
    @Singleton
    fun provideDeviceDetailsDao(casDatabase: CasDatabase): DeviceDetailsDao =
        casDatabase.deviceDetailsDataDao()

    @Provides
    @Singleton
    fun provideLogsRepo(
        logsDao: LogsDao,
        loggerService: LoggerService
    ): LogRepo = LogRepo(
        loggerDao = logsDao,
        loggerService = loggerService
    )

    @Provides
    @Singleton
    fun provideLogger(logRepo: LogRepo): MyLogger = MyLogger(
        logRepo = logRepo
    )

    @Provides
    @Singleton
    fun provideMqttClient(
        logger: MyLogger
    ) : CasMqttClient = CasMqttClient (
            myLogger = logger
            )

    @Provides
    @Singleton
    fun provideDatabase(app: Application): CasDatabase =
        Room.databaseBuilder(app, CasDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
}