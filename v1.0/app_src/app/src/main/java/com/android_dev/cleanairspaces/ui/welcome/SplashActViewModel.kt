package com.android_dev.cleanairspaces.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.work.*
import com.android_dev.cleanairspaces.bg_work.RefreshLocationsWorker
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.utils.DATA_REFRESHER_WORKER_NAME
import com.android_dev.cleanairspaces.utils.DATA_REFRESH_INTERVAL_MIN
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SplashActViewModel @Inject constructor(
        private val dataStoreManager: DataStoreManager,
        private val workManager: WorkManager,
        private val myLogger: MyLogger
) : ViewModel() {

    private val TAG = SplashActViewModel::class.java.simpleName

    fun getMapSelected(): LiveData<String?> = dataStoreManager.getSelectedMap().asLiveData()


    fun initDataRefresh() {
        myLogger.logThis(
                TAG, "initDataRefresh()", "calling worker"
        )


        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val refreshMapDataRequest =
                PeriodicWorkRequestBuilder<RefreshLocationsWorker>(
                        DATA_REFRESH_INTERVAL_MIN,
                        TimeUnit.MINUTES
                )
                        .addTag(DATA_REFRESHER_WORKER_NAME)
                        .setConstraints(constraints)
                        .build()

        workManager.enqueueUniquePeriodicWork(
                DATA_REFRESHER_WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                refreshMapDataRequest
        )

    }
}