package com.android_dev.cleanairspaces.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.android_dev.cleanairspaces.bg_work.RefreshMapDataWorker
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.utils.MAP_DATA_REFRESHER_WORKER
import com.android_dev.cleanairspaces.utils.MAP_DATA_REFRESH_INTERVAL_MIN
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SplashActViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val workManager: WorkManager
) : ViewModel() {

    private val TAG = SplashActViewModel::class.java.simpleName

    fun getMapSelected(): LiveData<String?> = dataStoreManager.getSelectedMap().asLiveData()


    fun initDataRefresh() {
        MyLogger.logThis(
            TAG, "initDataRefresh()", "calling worker"
        )

        //TODO .setRequiredNetworkType(NetworkType.CONNECTED)
        val constraints = Constraints.Builder()
            .build()

        val refreshMapDataRequest =
            PeriodicWorkRequestBuilder<RefreshMapDataWorker>(
                MAP_DATA_REFRESH_INTERVAL_MIN,
                TimeUnit.MINUTES
            )
                .addTag(MAP_DATA_REFRESHER_WORKER)
                .setConstraints(constraints)
                .build()

        workManager.enqueue(
            listOf(
                refreshMapDataRequest
            )
        )

    }
}