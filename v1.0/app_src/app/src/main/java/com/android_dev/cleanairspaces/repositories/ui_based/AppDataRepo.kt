package com.android_dev.cleanairspaces.repositories.ui_based

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android_dev.cleanairspaces.persistence.api.responses.*
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.DEVICE_INFO_METHOD
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.MONITOR_HISTORY_METHOD
import com.android_dev.cleanairspaces.persistence.api.services.InDoorLocationApiService
import com.android_dev.cleanairspaces.persistence.api.services.LocationHistoriesService
import com.android_dev.cleanairspaces.persistence.local.models.dao.*
import com.android_dev.cleanairspaces.persistence.local.models.entities.*
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import com.android_dev.cleanairspaces.utils.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataRepo
@Inject constructor(
    private val mapDataDao: MapDataDao,
    private val searchSuggestionsDataDao: SearchSuggestionsDataDao,
    private val watchedLocationHighLightsDao: WatchedLocationHighLightsDao,
    private val locationHistoryThreeDaysDao: LocationHistoryThreeDaysDao,
    private val locationHistoryWeekDao: LocationHistoryWeekDao,
    private val locationHistoryMonthDao: LocationHistoryMonthDao,
    private val locationHistoryUpdatesTrackerDao: LocationHistoryUpdatesTrackerDao,
    private val locationHistoriesService: LocationHistoriesService,
    private val inDoorLocationsApiService: InDoorLocationApiService,
    private val myLogger: MyLogger,
    private val monitorDetailsDataDao: MonitorDetailsDataDao,
    private val deviceDetailsDao: DeviceDetailsDao
) {

    /************** CACHES ************* */
    //shared across fragments
    data class WatchedLocationWithAqi(
        val aqiIndex: String?,
        val watchedLocationHighLights: WatchedLocationHighLights
    )

    val watchedLocationWithAqi = MutableLiveData<WatchedLocationWithAqi>()
    fun setCurrentlyWatchedLocationWithAQI(
        locationHighLights: WatchedLocationHighLights,
        aqiIndex: String?
    ) {
        watchedLocationWithAqi.value = WatchedLocationWithAqi(
            aqiIndex = aqiIndex,
            watchedLocationHighLights = locationHighLights
        )
    }

    private val TAG = AppDataRepo::class.java.simpleName


    fun getSearchSuggestions(query: String): Flow<List<SearchSuggestionsData>> {
        return searchSuggestionsDataDao.getSearchSuggestions(
            query = "%$query%"
        )
    }

    fun getMapDataFlow() = mapDataDao.getMapDataFlow()
    fun getWatchedLocationHighLights() = watchedLocationHighLightsDao.getWatchedLocationHighLights()


    suspend fun watchALocation(watchedLocationHighLight: WatchedLocationHighLights) {
        watchedLocationHighLightsDao.insertLocation(watchedLocationHighLight)
    }

    suspend fun stopWatchingALocation(watchedLocationHighLight: WatchedLocationHighLights) {
        watchedLocationHighLightsDao.deleteWatchedLocationHighLights(watchedLocationHighLight)
    }


    fun getLastDaysHistory(dataTag: String): Flow<List<LocationHistoryThreeDays>> {
        return locationHistoryThreeDaysDao.getLastDaysHistoryFlow(dataTag)
    }

    fun getLastWeekHistory(dataTag: String): Flow<List<LocationHistoryWeek>> {
        return locationHistoryWeekDao.getLastWeekHistoryFlow(dataTag)
    }

    fun getLastMonthHistory(dataTag: String): Flow<List<LocationHistoryMonth>> {
        return locationHistoryMonthDao.getLastMonthsHistoryFlow(dataTag)
    }

    suspend fun getLastTimeUpdatedHistory(dataTag: String): Long? {
        return locationHistoryUpdatesTrackerDao.checkLastUpdate(dataTag)
    }


    /******************** DATA API **********/
    private fun getLocationHistoryCallback(isMonitor: Boolean = false): Callback<LocationHistoriesResponse> {
        return object : Callback<LocationHistoriesResponse> {
            override fun onResponse(
                call: Call<LocationHistoriesResponse>,
                response: Response<LocationHistoriesResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            if (responseBody?.payload != null) {
                                if (isMonitor) {
                                    unEncryptMonitorHistoryPayload(
                                        pl = responseBody.payload,
                                        lTime = responseBody.ltime ?: "0"
                                    )
                                } else {
                                    unEncryptHistoryPayload(
                                        pl = responseBody.payload,
                                        lTime = responseBody.ltime ?: "0"
                                    )
                                }
                            }
                        } catch (exc: Exception) {
                            CoroutineScope(Dispatchers.IO).launch {
                                myLogger.logThis(
                                    tag = LogTags.EXCEPTION,
                                    from = "$TAG getLocationHistoryCallback()",
                                    msg = exc.message,
                                    exc = exc
                                )
                            }
                        }
                    }
                    else -> {
                    }
                }
            }

            override fun onFailure(call: Call<LocationHistoriesResponse>, t: Throwable) {
                CoroutineScope(Dispatchers.IO).launch {
                    myLogger.logThis(
                        tag = LogTags.EXCEPTION,
                        from = "$TAG getLocationHistoryCallback() - onFailure()",
                        msg = "${t.message}"
                    )
                }

            }

        }
    }

    private fun unEncryptHistoryPayload(pl: String, lTime: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataMatchingLTime =
                    recentRequestsData.filter { it.get(L_TIME_KEY).asString.equals(lTime) }
                if (!dataMatchingLTime.isNullOrEmpty()) {
                    val requestedData = dataMatchingLTime[0]
                    recentRequestsData.remove(requestedData)
                    val actualDataTag = requestedData.get(API_LOCAL_DATA_BINDER_KEY).asString

                    val unEncryptedPayload: String =
                        CasEncDecQrProcessor.decodeApiResponse(pl)
                    val unEncJson = JSONObject(unEncryptedPayload)


                    /** last 72 hours history */
                    val sevenTwoHrsJsonArray =
                        unEncJson.getJSONArray(LocationHistoriesResponse.daysResponseKey)


                    val sevenTwoHrsArrList = arrayListOf<LocationHistoryThreeDays>()
                    val sevenTwoHrsTotal = sevenTwoHrsJsonArray.length()
                    var i = 0
                    while (i < sevenTwoHrsTotal) {
                        val daysData =
                            Gson().fromJson(
                                sevenTwoHrsJsonArray.getJSONObject(i).toString(),
                                HistoryDataUnEncrypted::class.java
                            )
                        if (daysData.date_reading != null) {
                            val parsedDaysData = LocationHistoryThreeDays(
                                autoId = 0,
                                data = HistoryData(
                                    actualDataTag = actualDataTag,
                                    dates = daysData.date_reading!!,
                                    indoor_pm = (daysData.avg_reading ?: 0.0).toFloat(),
                                    outdoor_pm = (daysData.reading_comp ?: 0.0).toFloat(),
                                    temperature = (daysData.avg_temperature
                                        ?: 0.0).toFloat(),
                                    co2 = (daysData.avg_co2 ?: 0.0).toFloat(),
                                    tvoc = (daysData.avg_tvoc ?: "0.0").toFloat(),
                                    humidity = (daysData.avg_humidity ?: 0.0).toFloat(),
                                )
                            )
                            sevenTwoHrsArrList.add(parsedDaysData)
                        }
                        i++
                    }


                    /** last week history */
                    val weekJsonArray =
                        unEncJson.getJSONArray(LocationHistoriesResponse.weekResponseKey)
                    val weekArrList = arrayListOf<LocationHistoryWeek>()
                    val weekTotal = weekJsonArray.length()
                    var j = 0
                    while (j < weekTotal) {
                        val weekData =
                            Gson().fromJson(
                                weekJsonArray.getJSONObject(j).toString(),
                                HistoryDataUnEncrypted::class.java
                            )
                        if (weekData.date_reading != null) {
                            val parsedWeekData = LocationHistoryWeek(
                                autoId = 0,
                                data = HistoryData(
                                    actualDataTag = actualDataTag,
                                    dates = weekData.date_reading!!,
                                    indoor_pm = (weekData.avg_reading ?: 0.0).toFloat(),
                                    outdoor_pm = (weekData.reading_comp ?: 0.0).toFloat(),
                                    temperature = (weekData.avg_temperature
                                        ?: 0.0).toFloat(),
                                    co2 = (weekData.avg_co2 ?: 0.0).toFloat(),
                                    tvoc = (weekData.avg_tvoc ?: "0.0").toFloat(),
                                    humidity = (weekData.avg_humidity ?: 0.0).toFloat(),
                                )
                            )
                            weekArrList.add(parsedWeekData)
                        }
                        j++
                    }

                    /** last month history */
                    val monthJsonArray =
                        unEncJson.getJSONArray(LocationHistoriesResponse.monthResponseKey)
                    val monthArrList = arrayListOf<LocationHistoryMonth>()
                    val monthTotal = monthJsonArray.length()
                    var k = 0
                    while (k < monthTotal) {
                        val monthData =
                            Gson().fromJson(
                                monthJsonArray.getJSONObject(k).toString(),
                                HistoryDataUnEncrypted::class.java
                            )
                        if (monthData.date_reading != null) {
                            val parsedMonthData = LocationHistoryMonth(
                                autoId = 0,
                                data = HistoryData(
                                    actualDataTag = actualDataTag,
                                    dates = monthData.date_reading!!,
                                    indoor_pm = (monthData.avg_reading ?: 0.0).toFloat(),
                                    outdoor_pm = (monthData.reading_comp ?: 0.0).toFloat(),
                                    temperature = (monthData.avg_temperature
                                        ?: 0.0).toFloat(),
                                    co2 = (monthData.avg_co2 ?: 0.0).toFloat(),
                                    tvoc = (monthData.avg_tvoc ?: "0.0").toFloat(),
                                    humidity = (monthData.avg_humidity ?: 0.0).toFloat(),
                                )
                            )
                            monthArrList.add(parsedMonthData)
                        }
                        k++
                    }

                    saveFetchedHistoryForLocation(
                        daysHistory = sevenTwoHrsArrList,
                        weekHistory = weekArrList,
                        monthHistory = monthArrList,
                        dataTag = actualDataTag
                    )
                }
            } catch (exc: Exception) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG unEncryptHistoryPayload()",
                    msg = exc.message,
                    exc = exc
                )
            }
        }

    }

    private fun unEncryptMonitorHistoryPayload(pl: String, lTime: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataMatchingLTime =
                    recentRequestsData.filter { it.get(L_TIME_KEY).asString.equals(lTime) }
                if (!dataMatchingLTime.isNullOrEmpty()) {
                    val requestedData = dataMatchingLTime[0]
                    recentRequestsData.remove(requestedData)
                    val actualDataTag = requestedData.get(API_LOCAL_DATA_BINDER_KEY).asString


                    val unEncryptedPayload: String =
                        CasEncDecQrProcessor.decodeApiResponse(pl)
                    val unEncJson = JSONObject(unEncryptedPayload)


                    /** last 72 hours history */
                    val sevenTwoHrsJsonArray =
                        unEncJson.getJSONArray(LocationHistoriesResponse.daysResponseKey)


                    val sevenTwoHrsArrList = arrayListOf<LocationHistoryThreeDays>()
                    val sevenTwoHrsTotal = sevenTwoHrsJsonArray.length()
                    var i = 0
                    while (i < sevenTwoHrsTotal) {
                        val daysData =
                            Gson().fromJson(
                                sevenTwoHrsJsonArray.getJSONObject(i).toString(),
                                MonitorHistoryDataUnEncrypted::class.java
                            )
                        if (daysData.date_reading != null) {
                            val parsedDaysData = LocationHistoryThreeDays(
                                autoId = 0,
                                data = HistoryData(
                                    actualDataTag = actualDataTag,
                                    dates = daysData.date_reading!!,
                                    indoor_pm = (daysData.reading ?: 0.0).toFloat(),
                                    outdoor_pm = (daysData.reading_comp ?: 0.0).toFloat(),
                                    temperature = (daysData.temperature
                                        ?: 0.0).toFloat(),
                                    co2 = (daysData.co2 ?: 0.0).toFloat(),
                                    tvoc = (daysData.tvoc ?: "0.0").toFloat(),
                                    humidity = (daysData.humidity ?: 0.0).toFloat(),
                                )
                            )
                            sevenTwoHrsArrList.add(parsedDaysData)
                        }
                        i++
                    }


                    /** last week history */
                    val weekJsonArray =
                        unEncJson.getJSONArray(LocationHistoriesResponse.weekResponseKey)
                    val weekArrList = arrayListOf<LocationHistoryWeek>()
                    val weekTotal = weekJsonArray.length()
                    var j = 0
                    while (j < weekTotal) {
                        val weekData =
                            Gson().fromJson(
                                weekJsonArray.getJSONObject(j).toString(),
                                MonitorHistoryDataUnEncrypted::class.java
                            )
                        if (weekData.date_reading != null) {
                            val parsedWeekData = LocationHistoryWeek(
                                autoId = 0,
                                data = HistoryData(
                                    actualDataTag = actualDataTag,
                                    dates = weekData.date_reading!!,
                                    indoor_pm = (weekData.reading ?: 0.0).toFloat(),
                                    outdoor_pm = (weekData.reading_comp ?: 0.0).toFloat(),
                                    temperature = (weekData.temperature
                                        ?: 0.0).toFloat(),
                                    co2 = (weekData.co2 ?: 0.0).toFloat(),
                                    tvoc = (weekData.tvoc ?: "0.0").toFloat(),
                                    humidity = (weekData.humidity ?: 0.0).toFloat(),
                                )
                            )
                            weekArrList.add(parsedWeekData)
                        }
                        j++
                    }

                    /** last month history */
                    val monthJsonArray =
                        unEncJson.getJSONArray(LocationHistoriesResponse.monthResponseKey)
                    val monthArrList = arrayListOf<LocationHistoryMonth>()
                    val monthTotal = monthJsonArray.length()
                    var k = 0
                    while (k < monthTotal) {
                        val monthData =
                            Gson().fromJson(
                                monthJsonArray.getJSONObject(k).toString(),
                                MonitorHistoryDataUnEncrypted::class.java
                            )
                        if (monthData.date_reading != null) {
                            val parsedMonthData = LocationHistoryMonth(
                                autoId = 0,
                                data = HistoryData(
                                    actualDataTag = actualDataTag,
                                    dates = monthData.date_reading!!,
                                    indoor_pm = (monthData.reading ?: 0.0).toFloat(),
                                    outdoor_pm = (monthData.reading_comp ?: 0.0).toFloat(),
                                    temperature = (monthData.temperature
                                        ?: 0.0).toFloat(),
                                    co2 = (monthData.co2 ?: 0.0).toFloat(),
                                    tvoc = (monthData.tvoc ?: "0.0").toFloat(),
                                    humidity = (monthData.humidity ?: 0.0).toFloat(),
                                )
                            )
                            monthArrList.add(parsedMonthData)
                        }
                        k++
                    }

                    saveFetchedHistoryForLocation(
                        daysHistory = sevenTwoHrsArrList,
                        weekHistory = weekArrList,
                        monthHistory = monthArrList,
                        dataTag = actualDataTag
                    )
                }
            } catch (exc: Exception) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG unEncryptHistoryPayload()",
                    msg = exc.message,
                    exc = exc
                )
            }
        }

    }

    private val recentRequestsData = arrayListOf<JsonObject>()
    private suspend fun saveFetchedHistoryForLocation(
        daysHistory: ArrayList<LocationHistoryThreeDays>,
        weekHistory: ArrayList<LocationHistoryWeek>,
        monthHistory: ArrayList<LocationHistoryMonth>,
        dataTag: String
    ) {
        try {
            if (daysHistory.isNotEmpty()) {
                locationHistoryThreeDaysDao.deleteAllHistoriesForData(dataTag)
                locationHistoryThreeDaysDao.insertHistories(daysHistory)
            }
            if (weekHistory.isNotEmpty()) {
                locationHistoryWeekDao.deleteAllHistoriesForData(dataTag)
                locationHistoryWeekDao.insertHistories(weekHistory)
            }
            if (monthHistory.isNotEmpty()) {
                locationHistoryMonthDao.deleteAllHistoriesForData(dataTag)
                locationHistoryMonthDao.insertHistories(monthHistory)
            }
            locationHistoryUpdatesTrackerDao.saveLastUpdate(
                LocationHistoryUpdatesTracker(
                    actualDataTag = dataTag,
                )
            )
        } catch (exc: Exception) {
            myLogger.logThis(
                tag = LogTags.EXCEPTION,
                from = "$TAG saveFetchedHistoryForLocation()",
                msg = exc.message,
                exc = exc
            )
        }
    }

    suspend fun refreshHistoryForLocation(
        compId: String,
        locId: String,
        timeStamp: String,
        payload: String,
        dataTag: String,
        userName: String,
        userPassword: String
    ) {
        try {
            val method = DEVICE_INFO_METHOD
            val data = JsonObject()
            data.addProperty(L_TIME_KEY, timeStamp)
            data.addProperty(PAYLOAD_KEY, payload)
            val response = locationHistoriesService.fetchLocationHistory(
                data = data,
                method = method
            )

            // keep track of this request data ---
            data.addProperty(COMP_ID_KEY, compId)
            data.addProperty(LOC_ID_KEY, locId)
            data.addProperty(USER_KEY, userName)
            data.addProperty(PASSWORD_KEY, userPassword)
            response.enqueue(
                getLocationHistoryCallback()
            )
            data.addProperty(API_LOCAL_DATA_BINDER_KEY, dataTag)
            recentRequestsData.add(data)
        } catch (exc: Exception) {
            myLogger.logThis(
                tag = LogTags.EXCEPTION,
                from = "$TAG refreshHistoryForLocation()",
                msg = exc.message,
                exc = exc
            )
        }
    }


    suspend fun addNewWatchedLocationFromOutDoorSearchData(
        outDoorInfo: SearchSuggestionsData
    ): Boolean {
        try {
            val tag = "${outDoorInfo.company_id}${outDoorInfo.location_id}"
            val foundData = watchedLocationHighLightsDao.checkIfIsWatchedLocation(tag)
            if (foundData.isNotEmpty())
                return true //already added
            watchedLocationHighLightsDao.insertLocation(
                WatchedLocationHighLights(
                    actualDataTag = tag,
                    lat = outDoorInfo.lat!!,
                    lon = outDoorInfo.lon!!,
                    pm_outdoor = 0.0, //to be refreshed
                    pm_indoor = null,
                    name = outDoorInfo.nameToDisplay,
                    logo = "",
                    location_area = outDoorInfo.nameToDisplay,
                    indoor_co2 = null,
                    indoor_humidity = null,
                    indoor_temperature = null,
                    indoor_voc = null,
                    energyMonth = null,
                    energyMax = null,
                    isIndoorLoc = false,
                    compId = outDoorInfo.company_id,
                    locId = outDoorInfo.location_id,
                    monitorId = "",
                    lastRecPwd = "",
                    lastRecUsername = "",
                    is_secure = outDoorInfo.is_secure
                )
            )
            return true
        } catch (exc: Exception) {
            myLogger.logThis(
                tag = LogTags.EXCEPTION,
                from = "$TAG addNewWatchedLocationFromOutDoorSearchData()",
                msg = exc.message,
                exc = exc
            )

            return false
        }
    }

    suspend fun addNewWatchedLocationFromScannedQrCode(
        locationDataFromQr: LocationDataFromQr? = null,
        monitorDataFromQr: LocationDataFromQr? = null,
        userPwd: String,
        userName: String,
    ): Boolean {

        try {
            val tag: String
            val locationData: LocationDataFromQr
            when {
                monitorDataFromQr != null -> {
                    //user has scanned a qr code from monitor -- saving the details
                    tag =
                        "${monitorDataFromQr.company_id}${monitorDataFromQr.location_id}${monitorDataFromQr.monitor_id}"
                    val foundData = watchedLocationHighLightsDao.checkIfIsWatchedLocation(tag)
                    if (foundData.isNotEmpty()) return true
                    locationData = monitorDataFromQr
                }
                locationDataFromQr != null -> {
                    //user has scanned a qr code for company-- saving the details
                    tag = "${locationDataFromQr.company_id}${locationDataFromQr.location_id}"
                    val foundData = watchedLocationHighLightsDao.checkIfIsWatchedLocation(tag)
                    if (foundData.isNotEmpty()) return true
                    locationData = locationDataFromQr
                }


                else -> return false
            }
            watchedLocationHighLightsDao.insertLocation(
                WatchedLocationHighLights(
                    actualDataTag = tag,
                    lat = 0.0,
                    lon = 0.0,
                    pm_outdoor = null,
                    pm_indoor = null,
                    name = locationData.company,
                    logo = locationData.logo,
                    location_area = locationData.location,
                    indoor_co2 = null,
                    indoor_humidity = null,
                    indoor_temperature = null,
                    indoor_voc = null,
                    energyMonth = null,
                    energyMax = null,
                    isIndoorLoc = true, //we do not know--- could be -- to be determined upon refresh
                    compId = locationData.company_id,
                    locId = locationData.location_id,
                    monitorId = locationData.monitor_id,
                    lastRecPwd = userPwd,
                    lastRecUsername = userName,
                    is_secure = locationData.is_secure
                )
            )
            return true
        } catch (exc: Exception) {
            myLogger.logThis(
                tag = LogTags.EXCEPTION,
                from = "$TAG addNewWatchedLocationFromScannedQrCode()",
                msg = exc.message,
                exc = exc
            )

            return false
        }
    }

    suspend fun addNewWatchedLocationFromInDoorSearchData(
        userName: String,
        password: String,
        inDoorInfo: SearchSuggestionsData,
        indoorDataResultListener: AsyncResultListener
    ) {
        try {
            //indoor loc only has company id
            fetchIndoorLocationsToWatch(
                indoorLocation = inDoorInfo,
                userName = userName,
                userPass = password,
                indoorDataResultListener = indoorDataResultListener
            )
        } catch (exc: Exception) {
            myLogger.logThis(
                tag = LogTags.EXCEPTION,
                from = "$TAG addNewWatchedLocationFromInDoorSearchData()",
                msg = exc.message,
                exc = exc
            )

            indoorDataResultListener.onComplete(isSuccess = false)
        }
    }


    /********** INDOOR ******/
    private suspend fun fetchIndoorLocationsToWatch(
        indoorLocation: SearchSuggestionsData,
        userName: String = "",
        userPass: String = "",
        indoorDataResultListener: AsyncResultListener
    ) {
        val timeStamp = System.currentTimeMillis().toString()
        val pl =
            CasEncDecQrProcessor.getEncryptedEncodedPayloadForIndoorLocationOverviewDetails(
                timeStamp,
                companyId = indoorLocation.company_id,
                userName = userName,
                userPass = userPass
            )
        val data = JsonObject()
        data.addProperty(L_TIME_KEY, timeStamp)
        data.addProperty(PAYLOAD_KEY, pl)
        val indoorDetailsResponse =
            inDoorLocationsApiService.fetchInDoorLocationsDetails(pl = data)
        indoorDetailsResponse.enqueue(object : Callback<IndoorLocationsDetailsResponse> {
            override fun onResponse(
                call: Call<IndoorLocationsDetailsResponse>,
                response: Response<IndoorLocationsDetailsResponse>
            ) {
                try {
                    val decodedResponse =
                        CasEncDecQrProcessor.decodeApiResponse(response.body()!!.payload!!)
                    val jsonArray = JSONArray(decodedResponse)
                    val locationsToWatch = ArrayList<WatchedLocationHighLights>()
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val indoorLocationExtraDetails = Gson().fromJson(
                            jsonObject.toString(),
                            IndoorLocationExtraDetails::class.java
                        )

                        val tag =
                            "${indoorLocation.company_id}${indoorLocationExtraDetails.location_id}"
                        locationsToWatch.add(
                            WatchedLocationHighLights(
                                actualDataTag = tag,
                                lat = indoorLocation.lat ?: 0.0,
                                lon = indoorLocation.lon ?: 0.0,
                                pm_outdoor = null,
                                pm_indoor = null,
                                name = indoorLocationExtraDetails.name_en,
                                logo = indoorLocationExtraDetails.logo,
                                location_area = indoorLocation.nameToDisplay,
                                indoor_co2 = null,
                                indoor_humidity = null,
                                indoor_temperature = null,
                                indoor_voc = null,
                                energyMonth = null,
                                energyMax = null,
                                isIndoorLoc = true,
                                compId = indoorLocation.company_id,
                                locId = indoorLocationExtraDetails.location_id,
                                monitorId = "",
                                lastRecPwd = userPass,
                                lastRecUsername = userName,
                                is_secure = indoorLocation.is_secure
                            )
                        )
                    }
                    //USER MUST SELECT WHICH ONES FROM THIS ...
                    indoorDataResultListener.onComplete(isSuccess = true, data = locationsToWatch)
                } catch (exc: Exception) {

                    CoroutineScope(Dispatchers.IO).launch {
                        myLogger.logThis(
                            tag = LogTags.EXCEPTION,
                            from = "$TAG fetchIndoorLocationsToWatch()",
                            msg = exc.message,
                            exc = exc
                        )
                    }
                    indoorDataResultListener.onComplete(isSuccess = false)
                }
            }

            override fun onFailure(call: Call<IndoorLocationsDetailsResponse>, t: Throwable) {

                CoroutineScope(Dispatchers.IO).launch {
                    myLogger.logThis(
                        tag = LogTags.EXCEPTION,
                        from = "$TAG _ fetchIndoorLocationsToWatch()",
                        msg = t.message
                    )
                }
                indoorDataResultListener.onComplete(isSuccess = false)
            }

        })
    }

    /************************* MONITORS ******************************/
    private fun unEncryptMonitorsPayload(
        payload: String,
        compId: String,
        locId: String,
        userName: String,
        userPass: String,
        watchedLocationTag: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val monitorDetails = ArrayList<MonitorDetails>()
                val decodedResponse = CasEncDecQrProcessor.decodeApiResponse(payload)
                val monitorDetailsResponseRoot =
                    Gson().fromJson(decodedResponse, MonitorDetailsResponseRoot::class.java)
                for ((key, entry) in monitorDetailsResponseRoot.monitor) {
                    val isWatched: Boolean =
                        monitorDetailsDataDao.checkIfIsWatched(monitorId = key).isNotEmpty()
                    monitorDetails.add(
                        MonitorDetails(
                            actualDataTag = "$compId$locId$key",
                            for_watched_location_tag = watchedLocationTag,
                            company_id = compId,
                            location_id = locId,
                            monitor_id = key,
                            indoor_temperature = entry.indoor?.temperature?.toDoubleOrNull(),
                            indoor_pm_25 = entry.indoor?.reading?.toDoubleOrNull(),
                            indoor_co2 = entry.indoor?.co2?.toDoubleOrNull(),
                            indoor_humidity = entry.indoor?.humidity?.toDoubleOrNull(),
                            indoor_name_en = entry.indoor?.name_en ?: "",
                            indoor_display_param = entry.indoor?.display_param,
                            indoor_tvoc = entry.indoor?.tvoc?.toDoubleOrNull(),
                            outdoor_pm = entry.outdoor?.outdoor_pm?.toDoubleOrNull(),
                            outdoor_display_param = entry.outdoor?.outdoor_display_param,
                            outdoor_name_en = entry.outdoor?.outdoor_name_en,
                            lastRecPwd = userPass,
                            lastRecUName = userName,
                            watch_location = isWatched
                        )
                    )

                }
                if (monitorDetails.isNotEmpty())
                    monitorDetailsDataDao.insertOrReplaceAll(monitorDetails.toList())
            } catch (exc: Exception) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG unEncryptMonitorsPayload",
                    msg = exc.message,
                    exc = exc
                )
            }
        }
    }

    fun observeMonitorsIWatch() = monitorDetailsDataDao.observeWatchedMonitors()
    fun observeMonitorsForLocation(locationsTag: String) =
        monitorDetailsDataDao.observeMonitorsForLocation(locationsTag = locationsTag)

    suspend fun fetchMonitorsForALocation(
        watchedLocationTag: String,
        compId: String,
        locId: String,
        username: String,
        password: String
    ) {
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
            inDoorLocationsApiService.fetchInDoorLocationsMonitors(pl = data)
        request.enqueue(object : Callback<IndoorMonitorsResponse> {
            override fun onResponse(
                call: Call<IndoorMonitorsResponse>,
                response: Response<IndoorMonitorsResponse>
            ) {
                try {
                    if (response.body()?.payload != null) {
                        unEncryptMonitorsPayload(
                            watchedLocationTag = watchedLocationTag,
                            payload = response.body()?.payload!!,
                            compId = compId,
                            locId = locId,
                            userName = username,
                            userPass = password
                        )
                    }
                } catch (exc: Exception) {
                    CoroutineScope(Dispatchers.IO).launch {
                        myLogger.logThis(
                            tag = LogTags.EXCEPTION,
                            from = "$TAG _ fetchMonitorsForALocation()_onResponse",
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
                        from = "$TAG _ fetchMonitorsForALocation()",
                        msg = t.message
                    )
                }
            }

        })
    }

    suspend fun toggleWatchAMonitor(monitor: MonitorDetails, watch: Boolean) {
        monitorDetailsDataDao.toggleIsWatched(
            watchLocation = watch,
            monitorsTag = monitor.actualDataTag
        )
    }

    suspend fun refreshHistoryForMonitor(
        compId: String,
        locId: String,
        monitorId: String,
        payload: String,
        timeStamp: String,
        userName: String,
        userPassword: String,
        dataTag: String
    ) {
        try {
            val method = MONITOR_HISTORY_METHOD
            val data = JsonObject()
            data.addProperty(L_TIME_KEY, timeStamp)
            data.addProperty(PAYLOAD_KEY, payload)
            val response = locationHistoriesService.fetchMonitorHistory(
                data = data,
                method = method
            )

            // keep track of this request data ---
            data.addProperty(COMP_ID_KEY, compId)
            data.addProperty(LOC_ID_KEY, locId)
            data.addProperty(MON_ID_KEY, monitorId)
            data.addProperty(USER_KEY, userName)
            data.addProperty(PASSWORD_KEY, userPassword)
            data.addProperty(API_LOCAL_DATA_BINDER_KEY, dataTag)
            response.enqueue(
                getLocationHistoryCallback(isMonitor = true)
            )


            recentRequestsData.add(data)
        } catch (exc: Exception) {
            myLogger.logThis(
                tag = LogTags.EXCEPTION,
                from = "$TAG refreshHistoryForMonitor()",
                msg = exc.message,
                exc = exc
            )
        }
    }


    /************************* DEVICES *******************************/
    suspend fun toggleWatchADevice(device: DevicesDetails, watch: Boolean) {
        deviceDetailsDao.toggleIsWatched(watchDevice = watch, devicesTag = device.actualDataTag)
    }

    fun observeDevicesForLocation(locationsTag: String): Flow<List<DevicesDetails>> {
        return deviceDetailsDao.observeDevicesForLocation(locationsTag = locationsTag)
    }

    fun fetchDevicesForALocation(
        watchedLocationTag: String,
        compId: String,
        locId: String,
        username: String,
        password: String,
        resultListener: AsyncResultListener
    ) {
        val timeStamp = System.currentTimeMillis().toString()
        val pl =
            CasEncDecQrProcessor.getEncryptedEncodedPayloadForDevices(
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
            inDoorLocationsApiService.fetchInDoorLocationsDevices(pl = data)
        request.enqueue(object : Callback<DevicesDetailsResponse> {
            override fun onResponse(
                call: Call<DevicesDetailsResponse>,
                response: Response<DevicesDetailsResponse>
            ) {
                try {
                    if (response.body()?.payload != null) {
                        unEncryptDevicesPayload(
                            watchedLocationTag = watchedLocationTag,
                            payload = response.body()?.payload!!,
                            compId = compId,
                            locId = locId,
                            userName = username,
                            userPass = password,
                            resultListener = resultListener
                        )
                    } else {
                        Log.d(
                            "here", "no payload"
                        )
                        //wrong password probably
                        resultListener.onComplete(isSuccess = false)
                    }
                } catch (exc: Exception) {
                    resultListener.onComplete(isSuccess = false)
                    CoroutineScope(Dispatchers.IO).launch {
                        myLogger.logThis(
                            tag = LogTags.EXCEPTION,
                            from = "$TAG _ fetchDevicesForALocation()_onResponse",
                            msg = exc.message,
                            exc = exc
                        )
                    }
                }
            }

            override fun onFailure(call: Call<DevicesDetailsResponse>, t: Throwable) {
                resultListener.onComplete(isSuccess = false)
                CoroutineScope(Dispatchers.IO).launch {
                    myLogger.logThis(
                        tag = LogTags.EXCEPTION,
                        from = "$TAG _ fetchDevicesForALocation()_onFailure",
                        msg = t.message
                    )
                }
            }
        })

    }

    private fun unEncryptDevicesPayload(
        watchedLocationTag: String,
        payload: String,
        compId: String,
        locId: String,
        userName: String,
        userPass: String,
        resultListener: AsyncResultListener
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val decodedResponse = CasEncDecQrProcessor.decodeApiResponse(payload)
                val devices = JSONArray(decodedResponse)
                val foundDevices = ArrayList<DevicesDetails>()
                var i = 0
                while (i < devices.length()) {
                    val aDevice =
                        Gson().fromJson(
                            devices.getJSONObject(i).toString(),
                            DevicesDetails::class.java
                        )
                    aDevice.actualDataTag = "$compId$locId${aDevice.id}"
                    aDevice.lastRecUname = userName
                    aDevice.lastRecPwd = userPass
                    aDevice.for_watched_location_tag = watchedLocationTag
                    i++
                    foundDevices.add(aDevice)
                }
                if (foundDevices.isNotEmpty())
                    deviceDetailsDao.insertOrReplaceAll(foundDevices.toList())

                resultListener.onComplete(isSuccess = true)
            } catch (exc: Exception) {
                resultListener.onComplete(isSuccess = false)
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG unEncryptDevicesPayload",
                    msg = exc.message,
                    exc = exc
                )
            }
        }
    }


}
