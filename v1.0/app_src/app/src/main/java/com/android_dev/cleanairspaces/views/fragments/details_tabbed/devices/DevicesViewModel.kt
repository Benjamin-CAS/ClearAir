package com.android_dev.cleanairspaces.views.fragments.details_tabbed.devices

import androidx.lifecycle.*
import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.AsyncResultListener
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private val areDevicesLoaded = MutableLiveData<Boolean>()
    fun observeDeviceLoading(): LiveData<Boolean> = areDevicesLoaded

    private val resultListener = object : AsyncResultListener {
        override fun onComplete(data: Any?, isSuccess: Boolean) {
            viewModelScope.launch(Dispatchers.Main) {
                areDevicesLoaded.value = isSuccess
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
        } catch (exc: Exception) {
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


}