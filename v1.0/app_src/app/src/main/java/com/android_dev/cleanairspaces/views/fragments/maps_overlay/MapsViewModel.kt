package com.android_dev.cleanairspaces.views.fragments.maps_overlay

import android.location.Location
import androidx.lifecycle.*
import com.amap.api.maps.model.Marker
import com.android_dev.cleanairspaces.persistence.api.mqtt.DeviceUpdateMqttMessage
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val repo: AppDataRepo,
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


    fun observeMapData() = repo.getMapDataFlow().asLiveData()
    fun observeWatchedLocations() = repo.getWatchedLocationHighLights().asLiveData()
    fun deleteLocation(location: WatchedLocationHighLights) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.stopWatchingALocation(location)
        }
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


    fun setWatchedLocationInCache(location: WatchedLocationHighLights, aqiIndex: String?) {
        repo.setCurrentlyWatchedLocationWithAQI(location, aqiIndex)
    }


    /************* DEVICES I WATCH *************/
    fun observeDevicesIWatch() =
        repo.observeDevicesIWatch().asLiveData()


    fun watchThisDevice(Devices: DevicesDetails, watchDevice: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleWatchADevice(Devices, watch = watchDevice)
        }
    }

    fun onToggleFreshAir(device: DevicesDetails, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedDevice = repo.onToggleFreshAir(device, status)
            withContext(Dispatchers.Main) {
                setMqttStatus(updatedDevice)
                sendRequestByHttp(updatedDevice)
            }
        }
    }

    fun onToggleFanSpeed(device: DevicesDetails, status: String, speed: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedDevice = repo.onToggleFanSpeed(device, status, speed)
            withContext(Dispatchers.Main) {
                setMqttStatus(updatedDevice)
                sendRequestByHttp(updatedDevice)
            }
        }
    }

    fun onToggleMode(device: DevicesDetails, toMode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedDevice = repo.onToggleMode(device, toMode)
            withContext(Dispatchers.Main) {
                setMqttStatus(updatedDevice)
                sendRequestByHttp(updatedDevice)
            }
        }
    }

    fun onToggleDuctFit(device: DevicesDetails, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedDevice = repo.onToggleDuctFit(device, status)
            withContext(Dispatchers.Main) {
                setMqttStatus(updatedDevice)
                sendRequestByHttp(updatedDevice)
            }
        }
    }

    private val mqttParamsToSend = MutableLiveData<DeviceUpdateMqttMessage?>(null)
    fun getMqttMessage(): LiveData<DeviceUpdateMqttMessage?> = mqttParamsToSend
    private var lastUpdateDevice: DevicesDetails? = null
    fun setMqttStatus(updatedDevice: DevicesDetails?) {
        if (updatedDevice == null) {
            //reset
            mqttParamsToSend.value = null
        } else {
            lastUpdateDevice = updatedDevice
            lastUpdateDevice?.let {
                val param =
                    if (DevicesTypes.getDeviceInfoByType(it.device_type)?.hasDuctFit == true)
                        "${it.fan_speed}${it.df}${it.mode}"
                    else "${it.fan_speed}${it.fa}${it.mode}"
                mqttParamsToSend.value = DeviceUpdateMqttMessage(
                    device_mac_address = it.mac.trim(),
                    param = param
                )
            }
        }
    }
    fun refreshDevicesAfterDelay() {
        viewModelScope.launch(Dispatchers.IO) {
            lastUpdateDevice?.let {
                //refreshing twice ---
                delay(REFRESHED_DEVICE_DELAY)
                repo.refreshWatchedDevices()
                delay(REFRESHED_DEVICE_DELAY)
                repo.refreshWatchedDevices()
            }
        }
    }

    private fun sendRequestByHttp(device: DevicesDetails) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateDeviceStatusByHttp(
                device = device
            )
        }
    }

    fun stopWatchingDevice(device: DevicesDetails) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleWatchADevice(device =device, watch = false)
        }
    }


}
