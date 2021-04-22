package com.android_dev.cleanairspaces.bg_work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class RefreshMapDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val appDataRepo: AppDataRepo
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = RefreshMapDataWorker::class.java.simpleName

    override suspend fun doWork(): Result {

        withContext(Dispatchers.IO) {
            refreshMapData()
        }

        return Result.success()
    }

    private suspend fun refreshMapData() {
        MyLogger.logThis(
            TAG, "refreshMapData()", "refreshing"
        )
        appDataRepo.refreshMapData()
    }
}
