package com.android_dev.cleanairspaces.views.fragments.details_tabbed.monitors

import androidx.lifecycle.*
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.views.fragments.details_tabbed.details.DetailsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MonitorsViewModel
@Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val repo: AppDataRepo,
    private val myLogger: MyLogger
) : ViewModel() {

    var aqiIndex: String? = null
    private val TAG = DetailsViewModel::class.java.simpleName

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.getAqiIndex().collectLatest {
                withContext(Dispatchers.Main) {
                    aqiIndex = it
                }
            }
        }
    }

    fun observeWatchedLocation(): LiveData<WatchedLocationHighLights> = repo.watchedLocationHighLights

    /******************* MONITORS *************/
    fun observeMonitorsForLocation(locationsTag : String): LiveData<List<MonitorDetails>> {
        return repo.observeMonitorsForLocation(locationsTag = locationsTag).asLiveData()
    }
    fun fetchMonitorsForLocation(username: String, password: String) {
        try {
            val watchedLoc = repo.watchedLocationHighLights.value!!
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
        repo.toggleWatchAMonitor(monitor, watch = true)
    }



}