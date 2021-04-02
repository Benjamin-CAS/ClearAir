package com.cleanairspaces.android.ui.home.smart_qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanairspaces.android.models.repository.OutDoorLocationsRepo
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.QrCodeProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrCodeViewModel@Inject constructor(
    private val locationsRepo: OutDoorLocationsRepo
) : ViewModel(){

    private val TAG = QrCodeViewModel::class.java.simpleName

    fun addLocationFromCompanyInfo(locId: Int, compId: Int) {
       val timeStamp = System.currentTimeMillis().toString()
       val pl = QrCodeProcessor.getEncryptedEncodedPayload(locId, compId, timeStamp)
        viewModelScope.launch(Dispatchers.IO) {
            locationsRepo.fetchLocationFromScannedDeviceQr(base64Str = pl, payLoadTimeStamp = timeStamp)

        }
    }

    fun addLocationFromMonitorId(monitorId: String) {
        MyLogger.logThis(TAG, "addLocationFromMonitorId($monitorId)" , "called" )

    }
}