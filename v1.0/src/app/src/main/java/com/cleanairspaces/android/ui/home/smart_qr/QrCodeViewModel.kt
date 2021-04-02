package com.cleanairspaces.android.ui.home.smart_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cleanairspaces.android.models.entities.CustomerDeviceData
import com.cleanairspaces.android.models.repository.OutDoorLocationsRepo
import com.cleanairspaces.android.models.repository.ScannedDevicesRepo
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.QrCodeProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrCodeViewModel@Inject constructor(
    private val scannedDevicesRepo: ScannedDevicesRepo
) : ViewModel(){

    private val TAG = QrCodeViewModel::class.java.simpleName

    fun observeLocationFromCompanyInfo(compId: Int, locId: Int) : LiveData<CustomerDeviceData> = scannedDevicesRepo.getADeviceFlow(compId = compId.toString(), locId = locId.toString()).asLiveData()

    fun addLocationFromCompanyInfo(locId: Int, compId: Int) {
       val timeStamp = System.currentTimeMillis().toString()
       val pl = QrCodeProcessor.getEncryptedEncodedPayload(locId, compId, timeStamp)
        viewModelScope.launch(Dispatchers.IO) {
            scannedDevicesRepo.fetchLocationFromScannedDeviceQr(base64Str = pl, payLoadTimeStamp = timeStamp)
        }
    }

    fun addLocationFromMonitorId(monitorId: String) {
        MyLogger.logThis(TAG, "addLocationFromMonitorId($monitorId)" , "called" )
    }
}