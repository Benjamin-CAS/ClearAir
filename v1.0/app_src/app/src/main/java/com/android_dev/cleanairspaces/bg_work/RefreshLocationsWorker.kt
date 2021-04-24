package com.android_dev.cleanairspaces.bg_work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android_dev.cleanairspaces.repositories.api_facing.InDoorLocationsRepo
import com.android_dev.cleanairspaces.repositories.api_facing.LogRepo
import com.android_dev.cleanairspaces.repositories.api_facing.OutDoorLocationsRepo
import com.android_dev.cleanairspaces.repositories.api_facing.WatchedLocationUpdatesRepo
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@HiltWorker
class RefreshLocationsWorker @AssistedInject constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val outDoorLocationsRepo: OutDoorLocationsRepo,
        private val inDoorLocationsRepo: InDoorLocationsRepo,
        private val watchedLocationUpdatesRepo: WatchedLocationUpdatesRepo,
        private val myLogger: MyLogger,
        private val logRepo: LogRepo
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = RefreshLocationsWorker::class.java.simpleName
    private val TWO_MIN_MILLS = 180000L// 2 minutes

    override suspend fun doWork(): Result {

        withContext(Dispatchers.IO) {
            refreshOutDoorLocations()
            delay(TWO_MIN_MILLS)
            refreshInDoorLocations()
            delay(TWO_MIN_MILLS)
            sendLogData()
        }

        return Result.success()
    }

    private suspend fun refreshOutDoorLocations() {
        myLogger.logThis(
                TAG, "refreshOutDoorLocations", "refreshing"
        )
        outDoorLocationsRepo.refreshOutDoorLocations()
    }

    private suspend fun refreshInDoorLocations() {
        myLogger.logThis(
                TAG, "refreshInDoorLocations", "refreshing"
        )
        inDoorLocationsRepo.refreshInDoorLocations()
    }

    //doing it on resume is enough --- more actually!
    private suspend fun refreshWatchedLocations() {
        myLogger.logThis(
                TAG, "RefreshWatchedLocations", "refreshing"
        )
        watchedLocationUpdatesRepo.refreshWatchedLocationsData()
    }

    private suspend fun sendLogData(){
        logRepo.pushLogs()
    }

}
