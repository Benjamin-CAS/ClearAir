package com.android_dev.cleanairspaces.views.fragments.add_location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android_dev.cleanairspaces.persistence.api.responses.LocationDataFromQr
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.repositories.api_facing.LocationDetailsRepo
import com.android_dev.cleanairspaces.repositories.api_facing.WatchedLocationUpdatesRepo
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.AsyncResultListener
import com.android_dev.cleanairspaces.utils.CasEncDecQrProcessor
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class AddLocationViewModel @Inject constructor(
    private val appDataRepo: AppDataRepo,
    private val watchedLocationUpdatesRepo: WatchedLocationUpdatesRepo,
    private val locationDetailsRepo: LocationDetailsRepo,
    private val myLogger: MyLogger
) : ViewModel() {

    private val TAG = AddLocationViewModel::class.java.simpleName

    fun clearCache() {
        locationDetailsRepo.initCurrentlyScannedDeviceData()
    }

    fun observeScanQrCodeData(): LiveData<LocationDataFromQr> =
        locationDetailsRepo.getCurrentlyScannedDeviceData()

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
        val pl = CasEncDecQrProcessor.getEncryptedEncodedPayloadForScannedDeviceWithCompLoc(
            locId,
            compId,
            timeStamp
        )
        viewModelScope.launch(Dispatchers.IO) {
            locationDetailsRepo.fetchDataForScannedDeviceWithCompLoc(
                base64Str = pl,
                payLoadTimeStamp = timeStamp
            )
        }
    }

    private val locationIsAdded =
        MutableLiveData<WatchLocationProcessState>(WatchLocationProcessState.IDLE)

    fun observeAddProcess(): LiveData<WatchLocationProcessState> = locationIsAdded
    fun saveWatchedLocationFromScannedQr(
        locationDataFromQr: LocationDataFromQr? = null,
        monitorDataFromQr: LocationDataFromQr? = null,
        userPwd: String,
        userName: String
    ) {
        locationIsAdded.value = WatchLocationProcessState.ADDING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isSaved = appDataRepo.addNewWatchedLocationFromScannedQrCode(
                    locationDataFromQr = locationDataFromQr,
                    monitorDataFromQr = monitorDataFromQr,
                    userPwd = userPwd,
                    userName = userName
                )
                withContext(Dispatchers.Main) {
                    locationIsAdded.value = if (isSaved) {
                        WatchLocationProcessState.ADDED
                    } else WatchLocationProcessState.FAILED
                }
            } catch (exc: Exception) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG saveWatchedLocationFromScannedQr()",
                    msg = exc.message,
                    exc = exc
                )

            }
        }
    }

    fun saveWatchedOutdoorLocationSearchedInfo(
        outDoorInfo: SearchSuggestionsData
    ) {
        locationIsAdded.value = WatchLocationProcessState.ADDING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isSaved = appDataRepo.addNewWatchedLocationFromOutDoorSearchData(
                    outDoorInfo = outDoorInfo
                )
                withContext(Dispatchers.Main) {
                    locationIsAdded.value = if (isSaved) {
                        WatchLocationProcessState.ADDED
                    } else WatchLocationProcessState.FAILED
                }
            } catch (exc: Exception) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG saveWatchedOutdoorLocationSearchedInfo()",
                    msg = exc.message,
                    exc = exc
                )

            }
        }
    }

    private lateinit var allIndoorLocationsToChooseFrom: List<WatchedLocationHighLights>
    private val indoorLocationsToChooseFrom = MutableLiveData<List<WatchedLocationHighLights>>()
    fun observeIndoorLocationsToChooseFrom(): LiveData<List<WatchedLocationHighLights>> =
        indoorLocationsToChooseFrom

    private val indoorDataResultListener = object : AsyncResultListener {
        override fun onComplete(data: Any?, isSuccess: Boolean) {
            locationIsAdded.value = if (isSuccess && data != null && data is ArrayList<*>) {
                allIndoorLocationsToChooseFrom = data as ArrayList<WatchedLocationHighLights>
                indoorLocationsToChooseFrom.value = allIndoorLocationsToChooseFrom
                WatchLocationProcessState.ADDED_INDOOR
            } else WatchLocationProcessState.FAILED
        }

    }

    fun searchInIndoorLocations(query: String) {
        if (query.isNotBlank())
            indoorLocationsToChooseFrom.value =
                allIndoorLocationsToChooseFrom.filter { it.name.contains(query, ignoreCase = true) }
        else
            indoorLocationsToChooseFrom.value = allIndoorLocationsToChooseFrom
    }

    fun saveIndoorLocationFromFoundList(location: WatchedLocationHighLights) {
        val newList = allIndoorLocationsToChooseFrom.filter { it.name != location.name }
        allIndoorLocationsToChooseFrom = newList
        indoorLocationsToChooseFrom.value = newList
        viewModelScope.launch(Dispatchers.IO) {
            appDataRepo.watchALocation(location)
        }
    }

    fun resetWatchLocationState() {
        locationIsAdded.value = WatchLocationProcessState.IDLE
    }


    fun saveWatchedIndoorLocationSearchedInfo(
        userName: String,
        password: String,
        inDoorInfo: SearchSuggestionsData
    ) {
        locationIsAdded.value = WatchLocationProcessState.ADDING
        viewModelScope.launch(Dispatchers.IO) {
            try {
                appDataRepo.addNewWatchedLocationFromInDoorSearchData(
                    userName = userName,
                    password = password,
                    inDoorInfo = inDoorInfo,
                    indoorDataResultListener = indoorDataResultListener
                )

            } catch (exc: Exception) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG saveWatchedIndoorLocationSearchedInfo()",
                    msg = exc.message,
                    exc = exc
                )

            }
        }
    }

    fun refreshRecentlyAddedLocationDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            watchedLocationUpdatesRepo.refreshWatchedLocationsData()
        }
    }


}

enum class WatchLocationProcessState {
    IDLE,
    ADDING,
    ADDED,
    ADDED_INDOOR,
    FAILED
}