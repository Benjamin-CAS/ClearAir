package com.android_dev.cleanairspaces.ui.details

import androidx.lifecycle.*
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.persistence.local.models.entities.LocationHistoryMonth
import com.android_dev.cleanairspaces.persistence.local.models.entities.LocationHistoryThreeDays
import com.android_dev.cleanairspaces.persistence.local.models.entities.LocationHistoryWeek
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.repositories.ui_based.MapDataRepo
import com.android_dev.cleanairspaces.utils.HISTORY_EXPIRE_TIME_MILLS
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LocationDetailsViewModel
@Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val repo: MapDataRepo
) : ViewModel() {
    
    private val TAG  = LocationDetailsViewModel::class.java.simpleName

    var currentlyUsedAqi: String? = null
    lateinit var currentlyDisplayedLocationHighLights: WatchedLocationHighLights

    fun observeAQIIndex() = dataStoreManager.getAqiIndex().asLiveData()

    private val watchedLocationHighLights = MutableLiveData<WatchedLocationHighLights>()
    fun observeWatchedLocation(): LiveData<WatchedLocationHighLights> = watchedLocationHighLights
    fun setWatchedLocation(locationHighLights: WatchedLocationHighLights) {
        watchedLocationHighLights.value = locationHighLights
        currentlyDisplayedLocationHighLights = locationHighLights
        refreshHistoryIfNecessary(locationHighLights)
    }


    /******** history *****/
    lateinit var currentlyDisplayedDaysHistoryData: List<LocationHistoryThreeDays>
    lateinit var currentlyDisplayedMonthHistoryData: List<LocationHistoryMonth>
    lateinit var currentlyDisplayedWeekHistoryData: List<LocationHistoryWeek>


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



    fun observeHistories(): TripleHistoryFlow {
        val dataTag = currentlyDisplayedLocationHighLights.actualDataTag
        return TripleHistoryFlow(
            days = repo.getLastDaysHistory(dataTag).asLiveData(),
            week = repo.getLastWeekHistory(dataTag).asLiveData(),
            month = repo.getLastMonthHistory(dataTag).asLiveData()
        )
    }
    private fun refreshHistoryIfNecessary(location: WatchedLocationHighLights) {
        val dataTag = currentlyDisplayedLocationHighLights.actualDataTag
        viewModelScope.launch(Dispatchers.IO) {
            val lastUpdate = repo.getLastTimeUpdatedHistory(dataTag)
            val timeNow = System.currentTimeMillis()
            if (lastUpdate == null
                ||  hasExpired(timeNow, lastUpdate) ) {
                withContext(context = Dispatchers.Main) {
                    MyLogger.logThis(
                        TAG,
                        "refreshHistoryIfNecessary()", "refreshing history"
                    )
                    fetchHistory(
                        compId = location.compId,
                        locId = location.locId,
                        timeStamp = timeNow.toString(),
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


    private fun fetchHistory(compId : String, locId : String, timeStamp : String, dataTag : String, userName: String, userPassword: String) {
       //todo encrypt and fetch
    }


}

data class TripleHistoryFlow(
    val days: LiveData<List<LocationHistoryThreeDays>>,
    val week: LiveData<List<LocationHistoryWeek>>,
    val month: LiveData<List<LocationHistoryMonth>>
)

