package com.cleanairspaces.android.bg_work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cleanairspaces.android.models.repository.OutDoorLocationsRepo
import com.cleanairspaces.android.utils.MyLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class OutdoorLocationsRefresher @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationsRepo: OutDoorLocationsRepo
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = OutdoorLocationsRefresher::class.java.simpleName

    override suspend fun doWork(): Result {

        withContext(Dispatchers.IO) {
            refreshOutDoorLocations()
        }

        return Result.success()
    }

    private suspend fun refreshOutDoorLocations() {
        MyLogger.logThis(TAG, "refreshOutDoorLocations()", "called --")
        locationsRepo.refreshOutDoorLocations()
    }
}
