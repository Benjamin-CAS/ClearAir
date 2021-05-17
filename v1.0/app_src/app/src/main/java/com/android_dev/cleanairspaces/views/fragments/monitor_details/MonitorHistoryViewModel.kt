package com.android_dev.cleanairspaces.views.fragments.monitor_details

import android.util.Log
import androidx.lifecycle.*
import com.android_dev.cleanairspaces.persistence.local.models.entities.*
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.CasEncDecQrProcessor
import com.android_dev.cleanairspaces.utils.HISTORY_EXPIRE_TIME_MILLS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class MonitorHistoryViewModel @Inject constructor(
    private val repo: AppDataRepo,
) : ViewModel() {


    private val TAG = MonitorHistoryViewModel::class.java.simpleName
    var aqiIndex: String? = null


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

    fun refreshHistoryIfNecessary(monitorDetails: MonitorDetails) {
        val dataTag = monitorDetails.actualDataTag
        viewModelScope.launch(Dispatchers.IO) {
            val lastUpdate = repo.getLastTimeUpdatedHistory(dataTag)
            val timeNow = System.currentTimeMillis()
            if (lastUpdate == null || hasExpired(timeNow, lastUpdate)) {
                withContext(context = Dispatchers.Main) {
                    fetchMonitorHistory(
                        compId = monitorDetails.company_id,
                        locId = monitorDetails.location_id,
                        monitorId = monitorDetails.monitor_id,
                        timeStamp = timeNow.toString().replace(" ", ""),
                        dataTag = dataTag,
                        userName = monitorDetails.lastRecUName,
                        userPassword = monitorDetails.lastRecUName
                    )
                }
            }
        }
    }

    private fun hasExpired(timeNow: Long, lastUpdate: Long): Boolean {
        return (timeNow - lastUpdate) > HISTORY_EXPIRE_TIME_MILLS
    }


    private fun fetchMonitorHistory(
        compId: String,
        locId: String,
        timeStamp: String,
        dataTag: String,
        userName: String,
        userPassword: String,
        monitorId: String
    ) {
        val pl = CasEncDecQrProcessor.getEncryptedEncodedPayloadForMonitorHistory(
            compId = compId,
            locId = locId,
            monitorId = monitorId,
            userName = userName,
            userPassword = userPassword,
            timeStamp = timeStamp
        )

        viewModelScope.launch(Dispatchers.IO) {
            repo.refreshHistoryForMonitor(
                compId = compId,
                locId = locId,
                monitorId = monitorId,
                payload = pl,
                timeStamp = timeStamp,
                userName = userName,
                userPassword = userPassword,
                dataTag = dataTag
            )
        }
    }


}

data class TripleHistoryFlow(
    val days: LiveData<List<LocationHistoryThreeDays>>,
    val week: LiveData<List<LocationHistoryWeek>>,
    val month: LiveData<List<LocationHistoryMonth>>
)