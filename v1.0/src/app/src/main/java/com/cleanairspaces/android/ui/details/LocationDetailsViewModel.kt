package com.cleanairspaces.android.ui.details

import android.view.View
import android.widget.TextView
import androidx.lifecycle.*
import com.cleanairspaces.android.models.entities.*
import com.cleanairspaces.android.models.repository.ScannedDevicesRepo
import com.cleanairspaces.android.utils.HISTORY_EXPIRE_TIME_MILLS
import com.cleanairspaces.android.utils.MyLocationDetailsWrapper
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.QrCodeProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LocationDetailsViewModel @Inject constructor(
        private val scannedDevicesRepo: ScannedDevicesRepo
) : ViewModel() {

    private val TAG  = LocationDetailsViewModel::class.java.simpleName

    private val locationDetailsInfoLive = MutableLiveData<MyLocationDetailsWrapper>()
    private lateinit var locationDetailsInfo : MyLocationDetailsWrapper
    fun setCustomerDeviceDataDetailedNGetDeviceId(
        myLocationDetailsWrapper: MyLocationDetailsWrapper
    ): String {
        locationDetailsInfoLive.value = myLocationDetailsWrapper
        locationDetailsInfo = myLocationDetailsWrapper
        val location = myLocationDetailsWrapper.wrappedData.locationDetails
        val scannedDevice = myLocationDetailsWrapper.wrappedData.generalDataFromQr
        val (compId,locId,monitorId) = getCompIdLocIdMonitorId(scannedDevice)
        val forScannedDeviceId = createDeviceIdToBindTo(compId, locId, monitorId)
        refreshHistoryIfNecessary(
                compId,locId,forScannedDeviceId,location,
        )
        return forScannedDeviceId
    }
    fun getNonObservableDetails() : MyLocationDetailsWrapper = locationDetailsInfo
    fun observeLocationDetails(): LiveData<MyLocationDetailsWrapper> = locationDetailsInfoLive




    /******** history *****/
    lateinit var currentlyDisplayedDaysHistoryData: List<LocationHistoryThreeDays>
    lateinit var currentlyDisplayedMonthHistoryData: List<LocationHistoryMonth>
    lateinit var currentlyDisplayedWeekHistoryData: List<LocationHistoryWeek>
    private fun getCompIdLocIdMonitorId(scannedDevice: LocationDataFromQr): Triple<String, String, String> {
        return Triple(scannedDevice.company_id, scannedDevice.location_id,scannedDevice.monitor_id)
    }

    private val locationDaysHistoryLive = MutableLiveData<List<LocationHistoryThreeDays>>()
    fun observeLocationDaysHistory(): LiveData<List<LocationHistoryThreeDays>> = locationDaysHistoryLive
    fun setDaysHistory(history: List<LocationHistoryThreeDays>) {
            locationDaysHistoryLive.value = history
    }


    private val locationWeekHistoryLive = MutableLiveData<List<LocationHistoryWeek>>()
    fun observeLocationWeekHistory(): LiveData<List<LocationHistoryWeek>> = locationWeekHistoryLive
    fun setWeekHistory(history: List<LocationHistoryWeek>) {
        locationWeekHistoryLive.value = history
    }


    private val locationMonthHistoryLive = MutableLiveData<List<LocationHistoryMonth>>()
    fun observeLocationMonthHistory(): LiveData<List<LocationHistoryMonth>> = locationMonthHistoryLive
    fun setMonthHistory(history: List<LocationHistoryMonth>) {
       locationMonthHistoryLive.value =  history
    }



    fun observeHistories(forScannedDeviceId : String): TripleHistoryFlow {
        return TripleHistoryFlow(
                days = scannedDevicesRepo.getLastDaysHistory(forScannedDeviceId).asLiveData(),
                week = scannedDevicesRepo.getLastWeekHistory(forScannedDeviceId).asLiveData(),
                month = scannedDevicesRepo.getLastMonthHistory(forScannedDeviceId).asLiveData()
        )
    }
    private fun refreshHistoryIfNecessary(compId :String, locId : String, forScannedDeviceId : String, location: LocationDetails) {
        viewModelScope.launch(Dispatchers.IO) {
            val lastUpdate = scannedDevicesRepo.getLastTimeUpdatedHistory(forScannedDeviceId)
            val timeNow = System.currentTimeMillis()
            if (lastUpdate == null
                    ||  hasExpired(timeNow, lastUpdate) ) {
                withContext(Dispatchers.Main) {
                    MyLogger.logThis(
                            TAG,
                            "refreshHistoryIfNecessary()", "refreshing history"
                    )
                    fetchHistory(
                            compId = compId,
                            locId = locId,
                            timeStamps = timeNow.toString(),
                            forScannedDeviceId = forScannedDeviceId,
                            userName = location.lastKnownUserName,
                            userPassword = location.lastKnownPassword
                    )
                }
            }
        }
    }

    private fun hasExpired(timeNow: Long, lastUpdate: Long): Boolean {
        return (timeNow - lastUpdate) > HISTORY_EXPIRE_TIME_MILLS
    }


    private fun fetchHistory(compId : String, locId : String, timeStamps : String, forScannedDeviceId : String, userName: String, userPassword: String) {
        val timeStamp = "1619173011403"
        val pl = QrCodeProcessor.getEncryptedEncodedPayloadForDeviceDetails(
                compId = compId,
                locId = locId,
                userName = userName,
                userPassword = userPassword,
                timeStamp = timeStamp,
                showHistory = true
        )
        scannedDevicesRepo.fetchLocationHistory(
                compId = compId,
                locId = locId,
                payload = pl,
                lTime = timeStamp,
                userName = userName,
                userPassword = userPassword,
                forScannedDeviceId = forScannedDeviceId
        )
    }


}

data class TripleHistoryFlow(
        val days: LiveData<List<LocationHistoryThreeDays>>,
        val week: LiveData<List<LocationHistoryWeek>>,
        val month: LiveData<List<LocationHistoryMonth>>
)
