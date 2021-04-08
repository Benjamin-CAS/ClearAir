package com.cleanairspaces.android.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.cleanairspaces.android.models.api.CasDatabase
import com.cleanairspaces.android.models.api.OutDoorLocationsApiService
import com.cleanairspaces.android.models.api.QrScannedItemsApiService
import com.cleanairspaces.android.models.dao.CustomerDeviceDataDao
import com.cleanairspaces.android.models.dao.MyLocationDetailsDao
import com.cleanairspaces.android.models.dao.OutDoorLocationsDao
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

    //todo remove in production
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
    fun provideOutDoorLocationsApiService(retrofit: Retrofit): OutDoorLocationsApiService =
        retrofit.create(OutDoorLocationsApiService::class.java)

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
    fun provideOutDoorLocationsDao(casDatabase: CasDatabase): OutDoorLocationsDao =
        casDatabase.outDoorLocationsDao()


    @Provides
    @Singleton
    fun provideCustomerDeviceDataDao(casDatabase: CasDatabase): CustomerDeviceDataDao =
        casDatabase.customerDeviceDataDao()

    @Provides
    @Singleton
    fun provideMyLocationDetailsDao(casDatabase: CasDatabase): MyLocationDetailsDao =
        casDatabase.myLocationDetailsDao()


    @Provides
    @Singleton
    fun provideLocationsRepo(
        outDoorLocationsApiService: OutDoorLocationsApiService,
        coroutineScope: CoroutineScope,
        outDoorLocationsDao: OutDoorLocationsDao
    ): OutDoorLocationsRepo = OutDoorLocationsRepo(
        outDoorLocationsApiService = outDoorLocationsApiService,
        coroutineScope = coroutineScope,
        outDoorLocationsDao = outDoorLocationsDao
    )


    @Provides
    @Singleton
    fun provideScannedDevicesRepo(
        qrScannedItemsApiService: QrScannedItemsApiService,
        coroutineScope: CoroutineScope,
        customerDeviceDataDao: CustomerDeviceDataDao,
        myLocationDetailsDao: MyLocationDetailsDao
    ): ScannedDevicesRepo = ScannedDevicesRepo(
        qrScannedItemsApiService = qrScannedItemsApiService,
        coroutineScope = coroutineScope,
        customerDeviceDataDao = customerDeviceDataDao,
        myLocationDetailsDao = myLocationDetailsDao
    )

    @Singleton
    @Provides
    fun provideDataStoreMgr(
        @ApplicationContext context: Context
    ): DataStoreManager {
       return  DataStoreManager(context)
    }
}