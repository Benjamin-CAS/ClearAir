package com.cleanairspaces.android.ui.smart_qr

import androidx.lifecycle.*
import com.cleanairspaces.android.models.api.listeners.AsyncResultListener
import com.cleanairspaces.android.models.entities.LocationDataFromQr
import com.cleanairspaces.android.models.entities.createDeviceIdToBindTo
import com.cleanairspaces.android.models.repository.ScannedDevicesRepo
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

    fun observeLocationFromCompanyInfo(compId: Int, locId: Int): LiveData<LocationDataFromQr> =
        scannedDevicesRepo.getADeviceFlow(compId = compId.toString(), locId = locId.toString())
            .asLiveData()

    fun observeLocationFromMonitorInfo(monitorId: String): LiveData<LocationDataFromQr> =
        scannedDevicesRepo.getADeviceFlowByMonitorId(monitorId = monitorId).asLiveData()

    fun addLocationFromCompanyInfo(locId: Int, compId: Int) {
        val timeStamp = System.currentTimeMillis().toString()
        val pl = QrCodeProcessor.getEncryptedEncodedPayloadForLocation(locId, compId, timeStamp)
        viewModelScope.launch(Dispatchers.IO) {
            scannedDevicesRepo.fetchDataFromScannedDeviceQr(
                base64Str = pl,
                payLoadTimeStamp = timeStamp
            )
        }
    }

    fun addLocationFromMonitorId(monitorId: String) {
        val timeStamp = System.currentTimeMillis().toString()
        val pl = QrCodeProcessor.getEncryptedEncodedPayloadForMonitor(
            monitorId = monitorId,
            timeStamp = timeStamp
        )
        viewModelScope.launch(Dispatchers.IO) {
            scannedDevicesRepo.fetchDataFromScannedDeviceQr(
                base64Str = pl,
                payLoadTimeStamp = timeStamp,
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
    fun toggleLocationIsMine(
        locationDataFromQr: LocationDataFromQr,
        userName: String = "",
        userPassword: String = "",
        isMine: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isMine) {
                //user is adding this location -- refresh extra details of the location
                //and verify the user & password in the process
                val timeStamp = System.currentTimeMillis().toString()
                val compId = locationDataFromQr.company_id
                val locId = locationDataFromQr.location_id
                val monitorId = locationDataFromQr.monitor_id
                val forScannedDeviceId = createDeviceIdToBindTo(compId,locId,monitorId)
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
                    getMyLocationDataListener = getMyLocationDataListener,
                    userName = userName,
                    userPassword = userPassword,
                    forScannedDeviceId = forScannedDeviceId
                )
            } else {
                //user is not interested in details of this device
                    scannedDevicesRepo.removeMyDevice(locationDataFromQr)
            }
        }
    }


}