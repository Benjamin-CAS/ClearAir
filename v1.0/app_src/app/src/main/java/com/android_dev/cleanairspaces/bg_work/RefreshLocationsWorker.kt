package com.android_dev.cleanairspaces.bg_work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android_dev.cleanairspaces.persistence.local.models.dao.DeviceDetailsDao
import com.android_dev.cleanairspaces.repositories.api_facing.*
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.LogTags
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
    private val deviceDetailsDao: DeviceDetailsDao,
    private val appDataRepo: AppDataRepo,
    private val myLogger: MyLogger,
    private val logRepo: LogRepo,
    private val monitorDetailsUpdatesRepo: MonitorDetailsUpdatesRepo
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = RefreshLocationsWorker::class.java.simpleName

    override suspend fun doWork(): Result {

        withContext(Dispatchers.IO) {
            try {
                refreshOutDoorLocations()
                refreshInDoorLocations()
                refreshWatchedLocations()
                refreshWatchedMonitors()
                refreshWatchedDevices()
                sendLogData()
            } catch (exc: Exception) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG doWork()",
                    msg = exc.message,
                    exc = exc
                )
            }
        }

        return Result.success()
    }

    private suspend fun refreshWatchedMonitors() {
        monitorDetailsUpdatesRepo.refreshWatchedMonitors()
    }

    private suspend fun refreshWatchedLocations() {

        watchedLocationUpdatesRepo.refreshWatchedLocationsData()
    }

    private suspend fun refreshOutDoorLocations() {

        outDoorLocationsRepo.refreshOutDoorLocations()
    }

    private suspend fun refreshInDoorLocations() {

        inDoorLocationsRepo.refreshInDoorLocations()
    }

    private suspend fun refreshWatchedDevices(){
            try {
                val devices  = deviceDetailsDao.getAllDevicesNonObservable()
                for (device in devices) {
                  appDataRepo.fetchDevicesForALocation(
                        watchedLocationTag = device.for_watched_location_tag,
                        compId = device.compId,
                        locId = device.locId,
                        username = device.lastRecUname,
                        password = device.lastRecPwd,
                    )
                }
            } catch (exc: Exception) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG _ refreshWatchedDevices()",
                    msg = exc.message,
                    exc = exc
                )
            }
    }

    private suspend fun sendLogData() {
        val hourMills = 3600000L
        delay(hourMills)
        logRepo.pushLogs()
    }

}
