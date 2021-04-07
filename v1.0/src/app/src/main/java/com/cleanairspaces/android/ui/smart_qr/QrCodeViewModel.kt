package com.cleanairspaces.android.ui.smart_qr

import androidx.lifecycle.*
import com.cleanairspaces.android.models.api.listeners.AsyncResultListener
import com.cleanairspaces.android.models.entities.CustomerDeviceData
import com.cleanairspaces.android.models.repository.ScannedDevicesRepo
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.QrCodeProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrCodeViewModel @Inject constructor(
        private val scannedDevicesRepo: ScannedDevicesRepo
) : ViewModel() {

    private val TAG = QrCodeViewModel::class.java.simpleName

    fun observeLocationFromCompanyInfo(compId: Int, locId: Int): LiveData<CustomerDeviceData> =
            scannedDevicesRepo.getADeviceFlow(compId = compId.toString(), locId = locId.toString())
                    .asLiveData()

    fun observeLocationFromMonitorInfo(monitorId: String): LiveData<CustomerDeviceData> =
            scannedDevicesRepo.getADeviceFlowByMonitorId(monitorId = monitorId).asLiveData()

    fun addLocationFromCompanyInfo(locId: Int, compId: Int) {
        val timeStamp = System.currentTimeMillis().toString()
        val pl = QrCodeProcessor.getEncryptedEncodedPayloadForLocation(locId, compId, timeStamp)
        viewModelScope.launch(Dispatchers.IO) {
            scannedDevicesRepo.fetchDataFromScannedDeviceQr(
                    base64Str = pl,
                    payLoadTimeStamp = timeStamp,
                    forCompLocation = true
            )
        }
    }

    fun addLocationFromMonitorId(monitorId: String) {
        val timeStamp = System.currentTimeMillis().toString()
        val pl = QrCodeProcessor.getEncryptedEncodedPayloadForMonitor(
                monitorId = monitorId,
                timeStamp = timeStamp
        )
        MyLogger.logThis(TAG, "addLocationFromMonitorId($monitorId)", "called")
        viewModelScope.launch(Dispatchers.IO) {
            scannedDevicesRepo.fetchDataFromScannedDeviceQr(
                    base64Str = pl,
                    payLoadTimeStamp = timeStamp,
                    forCompLocation = false,
                    monitorId = monitorId
            )
        }
    }

    private val getMyLocationDataListener = object : AsyncResultListener {
        override fun onAsyncComplete(isSuccess: Boolean) {
            isMyLocationAddedSuccessful.value = isSuccess
        }

    }
    private val isMyLocationAddedSuccessful = MutableLiveData<Boolean>()
    fun observeMyLocationOperation(): LiveData<Boolean> = isMyLocationAddedSuccessful
    fun updateLocationIsMineStatus(
            customerDeviceData: CustomerDeviceData,
            userName: String = "",
            userPassword: String = "",
            isMine: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isMine) {
                //user is adding this location -- refresh extra details of the location
                //and verify the user & password in the process
                val timeStamp = System.currentTimeMillis().toString()
                val compId = customerDeviceData.company_id
                val locId = customerDeviceData.location_id
                val pl = QrCodeProcessor.getEncryptedEncodedPayloadForDeviceDetails(
                        compId = compId,
                        locId = locId,
                        userName = userName,
                        userPassword = userPassword,
                        timeStamp = timeStamp
                )
                scannedDevicesRepo.fetchDataForMyLocation(compId = compId, locId = locId, payload = pl, ltime = timeStamp, getMyLocationDataListener = getMyLocationDataListener)
            } else {
                scannedDevicesRepo.updateLocationIsMineStatus(customerDeviceData, isMine = isMine)
            }
        }
    }


}