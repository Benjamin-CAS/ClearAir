package com.cleanairspaces.android.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.work.*
import com.cleanairspaces.android.bg_work.IndoorLocationsRefresher
import com.cleanairspaces.android.bg_work.MyLocationsRefresher
import com.cleanairspaces.android.bg_work.OutdoorLocationsRefresher
import com.cleanairspaces.android.utils.DataStoreManager
import com.cleanairspaces.android.utils.LOCATIONS_REFRESH_WORKER
import com.cleanairspaces.android.utils.MY_LOCATIONS_REFRESH_RATE_MIN
import com.cleanairspaces.android.utils.OUTDOOR_LOCATIONS_REFRESH_RATE_MIN
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainActViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val workManager: WorkManager
) : ViewModel() {

    fun getSelectedMap(): LiveData<String> = dataStoreManager.getSelectedMap().asLiveData()


    val dataRefresherWorkerInfo: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosByTagLiveData(LOCATIONS_REFRESH_WORKER)

    fun refreshData() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val refreshOutDoorLocationsRequest =
            PeriodicWorkRequestBuilder<OutdoorLocationsRefresher>(
                OUTDOOR_LOCATIONS_REFRESH_RATE_MIN,
                TimeUnit.MINUTES
            )
                .addTag(LOCATIONS_REFRESH_WORKER)
                .setConstraints(constraints)
                .build()

        val refreshInDoorLocationsRequest =
            PeriodicWorkRequestBuilder<IndoorLocationsRefresher>(
                OUTDOOR_LOCATIONS_REFRESH_RATE_MIN,
                TimeUnit.MINUTES
            )
                .addTag(LOCATIONS_REFRESH_WORKER)
                .setConstraints(constraints)
                .build()

        val refreshMyLocationsRequest =
            PeriodicWorkRequestBuilder<MyLocationsRefresher>(
                MY_LOCATIONS_REFRESH_RATE_MIN,
                TimeUnit.MINUTES
            )
                .addTag(LOCATIONS_REFRESH_WORKER)
                .setConstraints(constraints)
                .build()

        workManager.enqueue(
            listOf(
                refreshInDoorLocationsRequest,
                refreshOutDoorLocationsRequest,
                refreshMyLocationsRequest
            )
        )

    }
}