package com.cleanairspaces.android.bg_work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cleanairspaces.android.models.repository.OutDoorLocationsRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class IndoorLocationsRefresher @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationsRepo: OutDoorLocationsRepo
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        withContext(Dispatchers.IO) {
            refreshInDoorLocations()
        }

        return Result.success()
    }

    private fun refreshInDoorLocations() {
        locationsRepo.refreshInDoorLocations()
    }
}
