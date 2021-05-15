package com.android_dev.cleanairspaces.repositories.api_facing

import android.util.Log
import com.android_dev.cleanairspaces.persistence.api.responses.IndoorMonitorsResponse
import com.android_dev.cleanairspaces.persistence.api.responses.MonitorDetailsResponseRoot
import com.android_dev.cleanairspaces.persistence.api.services.InDoorLocationApiService
import com.android_dev.cleanairspaces.persistence.local.models.dao.MonitorDetailsDataDao
import com.android_dev.cleanairspaces.utils.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitorDetailsUpdatesRepo
@Inject constructor(

        private val monitorDetailsDataDao: MonitorDetailsDataDao,
        private val inDoorLocationApiService: InDoorLocationApiService,
        private val myLogger: MyLogger
) {

    private val TAG = MonitorDetailsUpdatesRepo::class.java.simpleName

    suspend fun refreshWatchedMonitors() {
        try {
            val watchedMonitors = monitorDetailsDataDao.getAllWatchedMonitorsOnce()
            for (monitor in watchedMonitors) {
                refreshWatchedMonitor(
                        watchedLocationTag = monitor.for_watched_location_tag,
                        compId = monitor.company_id,
                        locId = monitor.location_id,
                        username = monitor.lastRecUName,
                        password = monitor.lastRecPwd,
                        forMonitorId = monitor.monitor_id
                )
            }
        } catch (exc: Exception) {
            myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG refreshWatchedMonitors()",
                    msg = exc.message,
                    exc = exc
            )
        }
    }

    private fun refreshWatchedMonitor(watchedLocationTag: String, compId: String, locId: String, username: String, password: String, forMonitorId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val timeStamp = System.currentTimeMillis().toString()
                val pl =
                        CasEncDecQrProcessor.getEncryptedEncodedPayloadForIndoorLocationMonitors(
                                timeStamp = timeStamp,
                                companyId = compId,
                                locId = locId,
                                userName = username,
                                userPass = password
                        )
                val data = JsonObject()
                data.addProperty(L_TIME_KEY, timeStamp)
                data.addProperty(PAYLOAD_KEY, pl)
                val request =
                        inDoorLocationApiService.fetchInDoorLocationsMonitors(pl = data)
                Log.d("refreshWatchedMonitor", "refreshing... $forMonitorId")
                request.enqueue(object : Callback<IndoorMonitorsResponse> {
                    override fun onResponse(call: Call<IndoorMonitorsResponse>, response: Response<IndoorMonitorsResponse>) {
                        try {
                            if (response.body()?.payload != null) {
                                unEncryptMonitorDetails(payload = response.body()!!.payload!!, forMonitorId = forMonitorId)
                            } else {
                                Log.d(
                                        "refreshWatchedMonitor", "with tag  $watchedLocationTag returned ${response.body()}"
                                )
                            }
                        } catch (exc: Exception) {
                            CoroutineScope(Dispatchers.IO).launch {
                                myLogger.logThis(
                                        tag = LogTags.EXCEPTION,
                                        from = "$TAG _ refreshWatchedMonitoration()_onResponse",
                                        msg = exc.message,
                                        exc = exc
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<IndoorMonitorsResponse>, t: Throwable) {
                        CoroutineScope(Dispatchers.IO).launch {
                            myLogger.logThis(
                                    tag = LogTags.EXCEPTION,
                                    from = "$TAG _ refreshWatchedMonitoration()",
                                    msg = t.message
                            )
                        }
                    }

                })
            } catch (exc: Exception) {
                CoroutineScope(Dispatchers.IO).launch {
                    myLogger.logThis(
                            tag = LogTags.EXCEPTION,
                            from = "$TAG refreshWatchedMonitor",
                            msg = exc.message,
                            exc = exc
                    )
                }
            }
        }
    }

    private fun unEncryptMonitorDetails(payload: String, forMonitorId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val decodedResponse = CasEncDecQrProcessor.decodeApiResponse(payload)
                val monitorDetailsResponseRoot = Gson().fromJson(decodedResponse, MonitorDetailsResponseRoot::class.java)
                Log.d("refreshWatchedMonitor", "found $forMonitorId")
                for ((key, entry) in monitorDetailsResponseRoot.monitor) {
                    if (key != forMonitorId) continue
                    else {
                        monitorDetailsDataDao.updateDetailsForMonitor(
                                id = forMonitorId,
                                co2 = entry.indoor?.co2?.toDoubleOrNull(),
                                inPm = entry.indoor?.reading?.toDoubleOrNull(),
                                tmp = entry.indoor?.temperature?.toDoubleOrNull(),
                                humid = entry.indoor?.humidity?.toDoubleOrNull(),
                                tvoc = entry.indoor?.tvoc?.toDoubleOrNull(),
                                inDisplayParam = entry.indoor?.display_param,
                                outPm = entry.outdoor?.outdoor_pm?.toDoubleOrNull(),
                                outDisplayParam = entry.outdoor?.outdoor_display_param,
                                updatedOn = System.currentTimeMillis()
                        )
                        Log.d("refreshWatchedMonitor", "updated monitor $forMonitorId")
                        break
                    }
                }
            } catch (exc: Exception) {
                myLogger.logThis(
                        tag = LogTags.EXCEPTION,
                        from = "$TAG unEncryptMonitorDetails()",
                        msg = exc.message,
                        exc = exc
                )
            }
        }
    }

}