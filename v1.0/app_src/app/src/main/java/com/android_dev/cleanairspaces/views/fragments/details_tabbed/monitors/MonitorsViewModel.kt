package com.android_dev.cleanairspaces.views.fragments.details_tabbed.monitors

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.views.fragments.details_tabbed.details.DetailsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonitorsViewModel
@Inject constructor(
        private val repo: AppDataRepo,
        private val myLogger: MyLogger
) : ViewModel() {

    var aqiIndex: String? = null
    private val TAG = MonitorsViewModel::class.java.simpleName

    fun observeWatchedLocationWithAqi(): LiveData<AppDataRepo.WatchedLocationWithAqi> = repo.watchedLocationWithAqi

    /******************* MONITORS *************/
    fun observeMonitorsForLocation(locationsTag: String): LiveData<List<MonitorDetails>> {
        return repo.observeMonitorsForLocation(locationsTag = locationsTag).asLiveData()
    }

    fun fetchMonitorsForLocation(username: String, password: String) {
        try {
            val watchedLoc = repo.watchedLocationWithAqi.value!!.watchedLocationHighLights

            viewModelScope.launch(Dispatchers.IO) {
                repo.fetchMonitorsForALocation(
                    watchedLocationTag = watchedLoc.actualDataTag,
                    compId = watchedLoc.compId,
                    locId = watchedLoc.locId,
                    username = username,
                    password = password
                )
            }
        } catch (exc: Exception) {
            viewModelScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                        tag = LogTags.EXCEPTION,
                        from = "$TAG _ fetchMonitorsForLocation($username: u-name, $password: pwd)",
                        msg = exc.message,
                        exc = exc
                )
            }
        }
    }

    fun watchThisMonitor(monitor: MonitorDetails, watchMonitor : Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleWatchAMonitor(monitor, watch = watchMonitor)
        }
    }


}