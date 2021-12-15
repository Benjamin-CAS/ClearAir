package com.android_dev.cleanairspaces.views.fragments.details_tabbed.devices

import android.util.Log
import androidx.lifecycle.*
import com.android_dev.cleanairspaces.persistence.api.mqtt.DeviceUpdateMqttMessage
import com.android_dev.cleanairspaces.persistence.local.models.entities.AirConditionerEntity
import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class DevicesViewModel
@Inject constructor(
    private val repo: AppDataRepo,
    private val myLogger: MyLogger
) : ViewModel() {
    var aqiIndex: String? = null
    private val TAG = DevicesViewModel::class.java.simpleName

    fun observeWatchedLocationWithAqi(): LiveData<AppDataRepo.WatchedLocationWithAqi> =
        repo.watchedLocationWithAqi

    /******************* Devices *************/
    fun observeDevicesForLocation(locationsTag: String): LiveData<List<DevicesDetails>> {
        return repo.observeDevicesForLocation(locationsTag = locationsTag).asLiveData()
    }

    private val areDevicesLoaded = MutableLiveData(DevicesLoadingState.IDLE)
    fun observeDeviceLoading(): LiveData<DevicesLoadingState> = areDevicesLoaded

    private val resultListener = object : AsyncResultListener {
        override fun onComplete(data: Any?, isSuccess: Boolean) {
            viewModelScope.launch(Dispatchers.Main) {
                if (isSuccess && data is Int) {
                    if (data > 0)
                        areDevicesLoaded.value = DevicesLoadingState.LOADED_SUCCESS
                    else
                        areDevicesLoaded.value = DevicesLoadingState.LOADED_SUCCESS_EMPTY
                } else
                    areDevicesLoaded.value = DevicesLoadingState.LOADING_FAILED
            }
        }
    }

    fun fetchDevicesForLocation(username: String, password: String) {
        try {
            val watchedLoc = repo.watchedLocationWithAqi.value!!.watchedLocationHighLights
            viewModelScope.launch(Dispatchers.IO) {
                repo.fetchDevicesForALocation(
                    watchedLocationTag = watchedLoc.actualDataTag,
                    compId = watchedLoc.compId,
                    locId = watchedLoc.locId,
                    username = username,
                    password = password,
                    resultListener = resultListener
                )
            }
            areDevicesLoaded.value = DevicesLoadingState.LOADING
        } catch (exc: Exception) {
            areDevicesLoaded.value = DevicesLoadingState.LOADING_FAILED
            viewModelScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG _ fetchDevicesForLocation($username: u-name, $password: pwd)",
                    msg = exc.message,
                    exc = exc
                )
            }
        }
    }
    fun watchThisDevice(Devices: DevicesDetails, watchDevice: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleWatchADevice(Devices, watch = watchDevice)
        }
    }
    fun watchThisAirConditioner(airConditionerEntity: AirConditionerEntity, b: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleWatchAirConditioner(airConditionerEntity,b)
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

    private var refreshedTimesAlready = 0
    fun refreshDevicesAfterDelay() {
        if(refreshedTimesAlready == 2) {
            refreshedTimesAlready = 0
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            lastUpdateDevice?.let {
                refreshedTimesAlready++
                delay(REFRESHED_DEVICE_MQTT_DELAY)
                withContext(Dispatchers.Main) {
                    fetchDevicesForLocation(
                        username = it.lastRecUname,
                        password = it.lastRecPwd
                    )
                }
                delay(REFRESHED_DEVICE_HTTP_DELAY)
                withContext(Dispatchers.Main){
                    refreshDevicesAfterDelay()
                }
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
    fun getAirConditionerDevices() = repo.getAirConditionerList()
    fun refreshAirConditioner(locationId:String){
        Log.e(TAG, "执行了这段代码: ")
        viewModelScope.launch {
            repo.insertAirConditionerDevices(locationId)
        }
    }
}

enum class DevicesLoadingState {
    IDLE,
    LOADING,
    LOADED_SUCCESS,
    LOADED_SUCCESS_EMPTY,
    LOADING_FAILED
}
