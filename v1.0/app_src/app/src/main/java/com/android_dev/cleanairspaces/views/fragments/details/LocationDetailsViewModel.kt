package com.android_dev.cleanairspaces.views.fragments.details

import androidx.lifecycle.*
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.persistence.local.models.entities.*
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.CasEncDecQrProcessor
import com.android_dev.cleanairspaces.utils.HISTORY_EXPIRE_TIME_MILLS
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LocationDetailsViewModel
@Inject constructor(
        private val dataStoreManager: DataStoreManager,
        private val repo: AppDataRepo,
        private val myLogger: MyLogger
) : ViewModel() {

    var aqiIndex: String? = null
    private val TAG = LocationDetailsViewModel::class.java.simpleName

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.getAqiIndex().collectLatest {
                withContext(Dispatchers.Main) {
                    aqiIndex = it
                }
            }
        }
    }
    var observableAqiIndex = dataStoreManager.getAqiIndex().asLiveData()

    private val watchedLocationHighLights = MutableLiveData<WatchedLocationHighLights>()
    fun observeWatchedLocation(): LiveData<WatchedLocationHighLights> = watchedLocationHighLights
    fun setWatchedLocation(locationHighLights: WatchedLocationHighLights) {
        watchedLocationHighLights.value = locationHighLights
        refreshHistoryIfNecessary(locationHighLights)
    }


    /******** history *****/
    var currentDatesForDaysChart: ArrayList<String> = ArrayList()
    var currentDatesForWeekChart: ArrayList<String> = ArrayList()
    var currentDatesForMonthChart: ArrayList<String> = ArrayList()

    lateinit var currentlyDisplayedDaysHistoryData: List<LocationHistoryThreeDays>
    lateinit var currentlyDisplayedMonthHistoryData: List<LocationHistoryMonth>
    lateinit var currentlyDisplayedWeekHistoryData: List<LocationHistoryWeek>


    private val locationDaysHistoryLive = MutableLiveData<List<LocationHistoryThreeDays>>()
    fun observeLocationDaysHistory(): LiveData<List<LocationHistoryThreeDays>> =
            locationDaysHistoryLive

    fun setDaysHistory(history: List<LocationHistoryThreeDays>) {
        locationDaysHistoryLive.value = history
    }


    private val locationWeekHistoryLive = MutableLiveData<List<LocationHistoryWeek>>()
    fun observeLocationWeekHistory(): LiveData<List<LocationHistoryWeek>> = locationWeekHistoryLive
    fun setWeekHistory(history: List<LocationHistoryWeek>) {
        locationWeekHistoryLive.value = history
    }


    private val locationMonthHistoryLive = MutableLiveData<List<LocationHistoryMonth>>()
    fun observeLocationMonthHistory(): LiveData<List<LocationHistoryMonth>> =
            locationMonthHistoryLive

    fun setMonthHistory(history: List<LocationHistoryMonth>) {
        locationMonthHistoryLive.value = history
    }


    fun observeHistories(dataTag: String): TripleHistoryFlow {
        return TripleHistoryFlow(
                days = repo.getLastDaysHistory(dataTag).asLiveData(),
                week = repo.getLastWeekHistory(dataTag).asLiveData(),
                month = repo.getLastMonthHistory(dataTag).asLiveData()
        )
    }

    private fun refreshHistoryIfNecessary(location: WatchedLocationHighLights) {
        val dataTag = location.actualDataTag
        viewModelScope.launch(Dispatchers.IO) {
            val lastUpdate = repo.getLastTimeUpdatedHistory(dataTag)
            val timeNow = System.currentTimeMillis()
            if (lastUpdate == null
                    || hasExpired(timeNow, lastUpdate)
            ) {
                withContext(context = Dispatchers.Main) {
                    fetchHistory(
                            compId = location.compId,
                            locId = location.locId,
                            timeStamp = timeNow.toString().replace(" ", ""),
                            dataTag = dataTag,
                            userName = location.lastRecUsername,
                            userPassword = location.lastRecPwd
                    )
                }
            }
        }
    }

    private fun hasExpired(timeNow: Long, lastUpdate: Long): Boolean {
        return (timeNow - lastUpdate) > HISTORY_EXPIRE_TIME_MILLS
    }


    private fun fetchHistory(
            compId: String,
            locId: String,
            timeStamp: String,
            dataTag: String,
            userName: String,
            userPassword: String
    ) {
        val pl = CasEncDecQrProcessor.getEncryptedEncodedPayloadForLocationHistory(
                compId = compId,
                locId = locId,
                userName = userName,
                userPassword = userPassword,
                timeStamp = timeStamp
        )
        repo.refreshHistoryForLocation(
                compId = compId,
                locId = locId,
                payload = pl,
                timeStamp = timeStamp,
                userName = userName,
                userPassword = userPassword,
                dataTag = dataTag
        )
    }

    /******************* MONITORS *************/
    fun observeMonitorsForLocation(locationsTag : String): LiveData<List<MonitorDetails>> {
           return repo.observeMonitorsForLocation(locationsTag = locationsTag).asLiveData()
    }
    fun fetchMonitorsForLocation(username: String, password: String) {
        try {
            val watchedLoc = watchedLocationHighLights.value!!
            repo.fetchMonitorsForALocation(
                    watchedLocationTag = watchedLoc.actualDataTag,
                    compId = watchedLoc.compId,
                    locId = watchedLoc.locId,
                    username = username,
                    password = password
            )
        }catch (exc : Exception){
            myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG _ fetchMonitorsForLocation($username: u-name, $password: pwd)",
                    msg = exc.message,
                    exc = exc
            )
        }
    }

    fun watchThisMonitor(monitor: MonitorDetails) {
        repo.watchMonitor(monitor, isWatched = true)
    }

}

data class TripleHistoryFlow(
        val days: LiveData<List<LocationHistoryThreeDays>>,
        val week: LiveData<List<LocationHistoryWeek>>,
        val month: LiveData<List<LocationHistoryMonth>>
)