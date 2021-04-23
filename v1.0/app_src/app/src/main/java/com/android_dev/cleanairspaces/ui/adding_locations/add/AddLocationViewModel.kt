package com.android_dev.cleanairspaces.ui.adding_locations.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android_dev.cleanairspaces.persistence.api.responses.LocationDataFromQr
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.repositories.api_facing.LocationDetailsRepo
import com.android_dev.cleanairspaces.repositories.api_facing.WatchedLocationUpdatesRepo
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.CasEncDecQrProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddLocationViewModel @Inject constructor(
        private val appDataRepo: AppDataRepo,
        private val watchedLocationUpdatesRepo: WatchedLocationUpdatesRepo,
        private val locationDetailsRepo: LocationDetailsRepo
) : ViewModel() {

    fun clearCache() {
        locationDetailsRepo.initCurrentlyScannedDeviceData()
    }

    fun observeScanQrCodeData(): LiveData<LocationDataFromQr> = locationDetailsRepo.getCurrentlyScannedDeviceData()

    fun fetchLocationDetailsForScannedMonitor(monitorId: String) {
        val timeStamp = System.currentTimeMillis().toString()
        val pl = CasEncDecQrProcessor.getEncryptedEncodedPayloadForScannedDeviceWithMonitorId(
                monitorId = monitorId,
                timeStamp = timeStamp
        )
        viewModelScope.launch(Dispatchers.IO) {
            locationDetailsRepo.fetchDataForScannedDeviceWithMonitorId(
                    base64Str = pl,
                    payLoadTimeStamp = timeStamp,
                    monitorId = monitorId
            )
        }
    }

    fun fetchLocationDetailsForScannedDeviceWithCompLoc(locId: Int, compId: Int) {
        val timeStamp = System.currentTimeMillis().toString()
        val pl = CasEncDecQrProcessor.getEncryptedEncodedPayloadForScannedDeviceWithCompLoc(locId, compId, timeStamp)
        viewModelScope.launch(Dispatchers.IO) {
            locationDetailsRepo.fetchDataForScannedDeviceWithCompLoc(
                    base64Str = pl,
                    payLoadTimeStamp = timeStamp
            )
        }
    }

    private val locationIsAdded = MutableLiveData<WatchLocationProcessState>(WatchLocationProcessState.IDLE)
    fun observeAddProcess(): LiveData<WatchLocationProcessState> = locationIsAdded
    fun saveWatchedLocationFromScannedQr(
            locationDataFromQr: LocationDataFromQr? = null, monitorDataFromQr: LocationDataFromQr? = null, userPwd: String, userName: String) {
        locationIsAdded.value = WatchLocationProcessState.ADDING
        viewModelScope.launch(Dispatchers.IO) {
            val isSaved = appDataRepo.addNewWatchedLocationFromScannedQrCode(
                    locationDataFromQr = locationDataFromQr, monitorDataFromQr = monitorDataFromQr, userPwd = userPwd, userName = userName)
            withContext(Dispatchers.Main) {
                locationIsAdded.value = if (isSaved) {
                    watchedLocationUpdatesRepo.refreshWatchedLocationsData()
                    WatchLocationProcessState.ADDED
                } else WatchLocationProcessState.FAILED
            }
        }
    }

    fun saveWatchedOutdoorLocationSearchedInfo(
            outDoorInfo: SearchSuggestionsData
    ) {
        locationIsAdded.value = WatchLocationProcessState.ADDING
        viewModelScope.launch(Dispatchers.IO) {
            val isSaved = appDataRepo.addNewWatchedLocationFromOutDoorSearchData(
                    outDoorInfo =  outDoorInfo)
            withContext(Dispatchers.Main) {
                locationIsAdded.value = if (isSaved) {
                    watchedLocationUpdatesRepo.refreshWatchedLocationsData()
                    WatchLocationProcessState.ADDED
                } else WatchLocationProcessState.FAILED
            }
        }
    }
}

enum class WatchLocationProcessState {
    IDLE,
    ADDING,
    ADDED,
    FAILED
}