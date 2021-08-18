package com.android_dev.cleanairspaces.views.fragments.welcome

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.work.*
import com.android_dev.cleanairspaces.bg_work.RefreshLocationsWorker
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.utils.DATA_REFRESHER_WORKER_NAME
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class SplashViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val workManager: WorkManager,
    private val myLogger: MyLogger
) : ViewModel() {

    companion object {
        const val TAG = "SplashViewModel"
    }

    /************** PERIODIC DATA REFRESHES -- SET BY THE MAIN ACTIVITY ITSELF *****************/
    fun getMapSelected(): LiveData<String?> = dataStoreManager.getSelectedMap().asLiveData()


    fun scheduleDataRefresh() {
        Log.e(TAG, "scheduleDataRefresh: 执行了")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val refreshDataRequest = PeriodicWorkRequestBuilder<RefreshLocationsWorker>(15L, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        // enqueueUniquePeriodicWork 防止重复
        workManager.enqueueUniquePeriodicWork(
            DATA_REFRESHER_WORKER_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            refreshDataRequest
        )
        workManager.getWorkInfoByIdLiveData(refreshDataRequest.id).observeForever {
                if (it != null && MyLogger.IS_DEBUG_MODE) {
                    Log.e(TAG, "scheduleDataRefresh: ----------")
                    Log.e(TAG, it.state.name)
                }
            }

    }

}