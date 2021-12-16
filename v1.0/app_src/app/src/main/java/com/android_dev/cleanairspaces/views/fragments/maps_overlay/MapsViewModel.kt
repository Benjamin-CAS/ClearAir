package com.android_dev.cleanairspaces.views.fragments.maps_overlay


import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.amap.api.maps.model.Marker
import com.android_dev.cleanairspaces.persistence.api.mqtt.DeviceUpdateMqttMessage
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.persistence.local.models.entities.AirConditionerEntity
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
    // 地图是否初始化完毕
    var mapHasBeenInitialized = MutableLiveData(false)
    // 语言
    fun observeMapLang() = dataStoreManager.getMapLang().asLiveData()

    var aqiIndex: String? = null

    // 空气指数
    fun observeAqiIndex() = dataStoreManager.getAqiIndex().asLiveData()


    fun observeMapData() = repo.getMapDataFlow().asLiveData()
    fun observeWatchedLocations() = repo.getWatchedLocationHighLights().asLiveData()
    fun deleteLocation(location: WatchedLocationHighLights) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.stopWatchingALocation(location)
        }
    }
    /**
     * 根据actualDataTag返回对应的设备详情列表
     */
    fun observeDevicesAllLocation(locationsTag: String) =
        repo.observeDevicesForLocation(locationsTag = locationsTag).asLiveData()

    fun deleteDevicesDetails(device: List<DevicesDetails>){
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteDevicesDetails(device)
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
    fun observeDevicesIWatch() = repo.observeDevicesIWatch().asLiveData()
    fun observeAirConditionerIWatch() = repo.observeAirConditionerIWatch().asLiveData()

    fun watchThisDevice(Devices: DevicesDetails, watchDevice: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleWatchADevice(Devices, watch = watchDevice)
        }
    }
    fun watchThisAirConditioner(airConditionerEntity: AirConditionerEntity, watchAirConditioner: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleWatchAirConditioner(airConditionerEntity,watchAirConditioner)
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
                Log.e(TAG, "onToggleFanSpeed: $updatedDevice")
                setMqttStatus(updatedDevice)
                sendRequestByHttp(updatedDevice)
            }
        }
    }

    fun onToggleMode(device: DevicesDetails, toMode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedDevice = repo.onToggleMode(device, toMode)
            Log.e(TAG, "onToggleMode: 模式切换数据:$updatedDevice")
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
                Log.d(TAG, "要发送的MQTT消息内容：$lastUpdateDevice")
                val param =
                    if (DevicesTypes.getDeviceInfoByType(it.device_type)?.hasDuctFit == true)
                        "${it.fan_speed}${it.df}${it.mode}"
                    else "${it.fan_speed}${it.fa}${it.mode}"
                Log.e(TAG, "setMqttStatus: MQTT MSG $param")
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
                delay(REFRESHED_DEVICE_MQTT_DELAY)
                repo.refreshWatchedDevices()
                delay(REFRESHED_DEVICE_HTTP_DELAY)
                repo.refreshWatchedDevices()
            }
        }
    }

    private fun sendRequestByHttp(device: DevicesDetails) {
        Log.e(TAG, "sendRequestByHttp: 发送了${device}")
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
    fun stopWatchingAirConditioner(airConditionerEntity: AirConditionerEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleWatchAirConditioner(airConditionerEntity,false)
        }
    }

    fun refreshAirConditioner(locationId:String){
        Log.e(TAG, "执行了这段代码: ")
        viewModelScope.launch {
            repo.insertAirConditionerDevices(locationId)
        }
    }
    companion object{
        var mapViewName = ""
    }

}
