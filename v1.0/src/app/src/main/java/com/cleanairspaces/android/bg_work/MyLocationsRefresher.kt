package com.cleanairspaces.android.bg_work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cleanairspaces.android.models.repository.ScannedDevicesRepo
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.QrCodeProcessor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class MyLocationsRefresher @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scannedDevicesRepo: ScannedDevicesRepo
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = MyLocationsRefresher::class.java.simpleName

    override suspend fun doWork(): Result {

        withContext(Dispatchers.IO) {
            refreshMyLocationsData()
        }

        return Result.success()
    }

    private suspend fun refreshMyLocationsData() {
        val myLocations = scannedDevicesRepo.getMyLocationsOnce()
        for (aLocation in myLocations) {
            val compId = aLocation.company_id
            val locId = aLocation.location_id
            val userName = aLocation.lastKnownUserName
            val userPassword = aLocation.lastKnownPassword
            val timeStamp = System.currentTimeMillis().toString() + aLocation.bound_to_scanned_device_id
            MyLogger.logThis(
                TAG, "refreshMyLocationsData()",
                "companyId $compId and locationId $locId",
            )
            val pl = QrCodeProcessor.getEncryptedEncodedPayloadForDeviceDetails(
                    compId = compId,
                    locId = locId,
                    userName = userName,
                    userPassword = userPassword,
                    timeStamp = timeStamp,
                    showHistory = true
            )
            scannedDevicesRepo.fetchLocationDetails(
                compId = compId,
                locId = locId,
                payload = pl,
                lTime = timeStamp,
                userName = userName,
                userPassword = userPassword,
                ignoreResultIfNotAlreadyMyLocation = true,
                forScannedDeviceId = aLocation.bound_to_scanned_device_id
            )
        }
    }
}
