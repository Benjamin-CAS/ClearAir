package com.android_dev.cleanairspaces.views.fragments.maps_overlay

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.amap.api.maps.model.Marker
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.LAT_LON_DELIMITER
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.USER_LOCATION_UPDATE_INTERVAL_MILLS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
        private val dataStoreManager: DataStoreManager,
        private val appDataRepo: AppDataRepo,
        private val myLogger: MyLogger
) : ViewModel() {

    private val TAG = MapsViewModel::class.java.simpleName

    var myLocMarkerOnAMap: Marker? = null
    var myLocMarkerOnGMap: com.google.android.gms.maps.model.Marker? = null
    var alreadyPromptedUserForLocationSettings = false

    var mapHasBeenInitialized = MutableLiveData<Boolean>(false)
    fun observeMapLang() = dataStoreManager.getMapLang().asLiveData()

    var aqiIndex: String? = null
    fun observeAqiIndex() = dataStoreManager.getAqiIndex().asLiveData()


    fun observeMapData() = appDataRepo.getMapDataFlow().asLiveData()
    fun observeWatchedLocations() = appDataRepo.getWatchedLocationHighLights().asLiveData()
    fun deleteLocation(location: WatchedLocationHighLights) {
        viewModelScope.launch(Dispatchers.IO) {
            appDataRepo.stopWatchingALocation(location)
        }
    }

    fun observeMonitorsIWatch() = appDataRepo.observeMonitorsIWatch().asLiveData()


    fun observeIfAlreadyAskedLocPermission() = dataStoreManager.hasAlreadyAskedLocPermission().asLiveData()
    fun setAlreadyAskedLocPermission() = viewModelScope.launch(Dispatchers.IO) {
        dataStoreManager.setAlreadyAskedLocPermission()
    }

    private var location: Location? = null
    fun getMyLocationOrNull() = location
    private var updatesStarted = false
    private var stopSendingLocUpdates = false
    fun onLocationChanged(newLocation: Location) {
        location = newLocation
        if (!updatesStarted) {
            updatesStarted = true
            viewModelScope.launch(Dispatchers.IO) {
                sendUserLoc()
            }
        }
    }

    private suspend fun sendUserLoc() {
        location?.let { loc ->
            myLogger.logThis(
                    tag = LogTags.USER_LOCATION_CHANGED,
                    from = TAG,
                    msg = "${loc.latitude}$LAT_LON_DELIMITER${loc.longitude}"
            )
        }
        delay(USER_LOCATION_UPDATE_INTERVAL_MILLS)
        if (!stopSendingLocUpdates) {
            sendUserLoc()
        }
    }

    override fun onCleared() {
        super.onCleared()
        updatesStarted = false
        stopSendingLocUpdates = true
    }

    fun stopWatchingMonitor(monitor: MonitorDetails) {
        viewModelScope.launch(Dispatchers.IO) {
            appDataRepo.toggleWatchAMonitor(monitor, watch = false)
        }
    }

    fun setWatchedLocationInCache(location: WatchedLocationHighLights, aqiIndex  :String?) {
        appDataRepo.setCurrentlyWatchedLocationWithAQI(location, aqiIndex)
    }

}