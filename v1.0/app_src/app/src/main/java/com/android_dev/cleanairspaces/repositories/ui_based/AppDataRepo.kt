package com.android_dev.cleanairspaces.repositories.ui_based

import com.android_dev.cleanairspaces.persistence.api.responses.*
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.DEVICE_INFO_METHOD
import com.android_dev.cleanairspaces.persistence.api.services.InDoorLocationApiService
import com.android_dev.cleanairspaces.persistence.api.services.LocationHistoriesService
import com.android_dev.cleanairspaces.persistence.local.models.dao.*
import com.android_dev.cleanairspaces.persistence.local.models.entities.*
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
    private val coroutineScope: CoroutineScope,
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
) {

    private val TAG = AppDataRepo::class.java.simpleName


    fun getSearchSuggestions(query: String): Flow<List<SearchSuggestionsData>> {
        return searchSuggestionsDataDao.getSearchSuggestions(
            query = "%$query%"
        )
    }

    fun getMapDataFlow() = mapDataDao.getMapDataFlow()
    fun getWatchedLocationHighLights() = watchedLocationHighLightsDao.getWatchedLocationHighLights()


    fun watchALocation(watchedLocationHighLight: WatchedLocationHighLights) {
        coroutineScope.launch(Dispatchers.IO) {
            watchedLocationHighLightsDao.insertLocation(watchedLocationHighLight)
        }
    }

    fun stopWatchingALocation(watchedLocationHighLight: WatchedLocationHighLights) {
        coroutineScope.launch(Dispatchers.IO) {
            watchedLocationHighLightsDao.deleteWatchedLocationHighLights(watchedLocationHighLight)
        }
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
    private fun getLocationHistoryCallback(): Callback<LocationHistoriesResponse> {
        return object : Callback<LocationHistoriesResponse> {
            override fun onResponse(
                call: Call<LocationHistoriesResponse>,
                response: Response<LocationHistoriesResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            if (responseBody == null) {
                                myLogger.logThis(
                                    TAG,
                                    "getLocationHistoryCallback -> onResponse()",
                                    "response is OK but body is null"
                                )
                            } else {
                                if (responseBody.payload != null) {
                                    unEncryptHistoryPayload(
                                        pl = responseBody.payload,
                                        lTime = responseBody.ltime ?: "0"
                                    )
                                } else {
                                    myLogger.logThis(
                                        TAG,
                                        "getLocationHistoryCallback -> onResponse()",
                                        "response is OK but payload is null - $responseBody"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            myLogger.logThis(
                                TAG,
                                "getLocationHistoryCallback -> onResponse()",
                                "response is OK but an exception occurred ${e.message}",
                                e
                            )
                        }
                    }
                    else -> {
                        myLogger.logThis(
                            TAG,
                            "getLocationHistoryCallback -> onResponse()",
                            "response code is not 200 OK $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<LocationHistoriesResponse>, t: Throwable) {
                myLogger.logThis(
                    TAG,
                    "getLocationHistoryCallback -> onFailure()",
                    "exc ${t.message}"
                )
            }

        }
    }

    private fun unEncryptHistoryPayload(pl: String, lTime: String) {
        try {
            val dataMatchingLTime =
                recentRequestsData.filter { it.get(L_TIME_KEY).asString.equals(lTime) }
            if (dataMatchingLTime.isNullOrEmpty()) return
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
                            temperature = (daysData.avg_temperature ?: 0.0).toFloat(),
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
            val weekJsonArray = unEncJson.getJSONArray(LocationHistoriesResponse.weekResponseKey)
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
                            temperature = (weekData.avg_temperature ?: 0.0).toFloat(),
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
            val monthJsonArray = unEncJson.getJSONArray(LocationHistoriesResponse.monthResponseKey)
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
                            temperature = (monthData.avg_temperature ?: 0.0).toFloat(),
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
        } catch (e: Exception) {
            myLogger.logThis(
                TAG,
                "unEncryptHistoryPayload(payload - $pl, lTime $lTime)",
                "exception thrown ${e.message}", e
            )
        }
    }

    private val recentRequestsData = arrayListOf<JsonObject>()
    private fun saveFetchedHistoryForLocation(
        daysHistory: ArrayList<LocationHistoryThreeDays>,
        weekHistory: ArrayList<LocationHistoryWeek>,
        monthHistory: ArrayList<LocationHistoryMonth>,
        dataTag: String
    ) {
        coroutineScope.launch(Dispatchers.IO) {
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
                myLogger.logThis(
                    TAG, "saveFetchedHistoryForLocation()", "success"
                )
            } catch (e: java.lang.Exception) {
                myLogger.logThis(
                    TAG, "saveFetchedHistoryForLocation()", "exception ${e.message}", e
                )
            }
        }
    }

    fun refreshHistoryForLocation(
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
            myLogger.logThis(
                TAG,
                "refreshHistoryForLocation()",
                "data passed $data $payload",
            )
        } catch (e: Exception) {
            myLogger.logThis(
                TAG,
                "refreshHistoryForLocation()",
                "exc ${e.message}",
                e
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
                    lastRecUsername = ""
                )
            )
            myLogger.logThis(
                TAG, "addNewWatchedLocationFromOutDoorSearchData()", "--done"
            )
            return true
        } catch (ex: java.lang.Exception) {
            myLogger.logThis(
                TAG,
                "addNewWatchedLocationFromOutDoorSearchData failed",
                "exception ${ex.message}",
                ex
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
            var tag = ""
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
                    lastRecUsername = userName
                )
            )
            myLogger.logThis(
                TAG, "addNewWatchedLocationFromScannedQrCode()", "--done"
            )
            return true
        } catch (ex: java.lang.Exception) {
            myLogger.logThis(
                TAG, "addNewWatchedLocationFromScannedQrCode failed", "exception ${ex.message}", ex
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
            myLogger.logThis(
                TAG, "addNewWatchedLocationFromInDoorSearchData()", "--done"
            )
        } catch (ex: java.lang.Exception) {
            myLogger.logThis(
                TAG,
                "addNewWatchedLocationFromInDoorSearchData failed",
                "exception ${ex.message}",
                ex
            )
            indoorDataResultListener.onComplete(isSuccess = false)
        }
    }


    /********** INDOOR ******/
    private fun fetchIndoorLocationsToWatch(
        indoorLocation: SearchSuggestionsData,
        userName: String = "",
        userPass: String = "",
        indoorDataResultListener: AsyncResultListener
    ) {
        //MORE -- DETAILS
        coroutineScope.launch(Dispatchers.IO) {
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
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val indoorLocationExtraDetails = Gson().fromJson(
                                jsonObject.toString(),
                                IndoorLocationExtraDetails::class.java
                            )
                            watchIndoorLocation(
                                indoorLocation = indoorLocation,
                                indoorLocationExtraDetails = indoorLocationExtraDetails,
                                userName = userName,
                                userPwd = userPass
                            )
                            myLogger.logThis(
                                TAG,
                                "refreshExtraDetails()",
                                "onResponse() found ${indoorLocationExtraDetails.name_en} with $indoorLocationExtraDetails"
                            )
                        }
                        myLogger.logThis(
                            TAG,
                            "refreshExtraDetails()",
                            "onResponse() decoded $decodedResponse"
                        )
                        indoorDataResultListener.onComplete(isSuccess = true)
                    } catch (exc: Exception) {
                        myLogger.logThis(
                            TAG,
                            "refreshExtraDetails()",
                            "onResponse() ${exc.message}",
                            exc
                        )
                        indoorDataResultListener.onComplete(isSuccess = false)
                    }
                }

                override fun onFailure(call: Call<IndoorLocationsDetailsResponse>, t: Throwable) {
                    myLogger.logThis(TAG, "refreshExtraDetails()", "onFailure() ${t.message}")
                    indoorDataResultListener.onComplete(isSuccess = false)
                }

            })
        }
    }

    private fun watchIndoorLocation(
        indoorLocation: SearchSuggestionsData,
        indoorLocationExtraDetails: IndoorLocationExtraDetails,
        userPwd: String,
        userName: String
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val tag = "${indoorLocation.company_id}${indoorLocationExtraDetails.location_id}"
                mapDataDao.insert(
                    WatchedLocationHighLights(
                        actualDataTag = tag,
                        lat = indoorLocation.lat ?: 0.0,
                        lon = indoorLocation.lon ?: 0.0,
                        pm_outdoor = null,
                        pm_indoor = null,
                        name = indoorLocationExtraDetails.name_en,
                        logo = indoorLocationExtraDetails.logo,
                        location_area = "",
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
                        lastRecPwd = userPwd,
                        lastRecUsername = userName
                    )
                )
                myLogger.logThis(
                    TAG,
                    "watchIndoorLocations()",
                    "saving $tag  ${indoorLocationExtraDetails.name_en} with ${indoorLocation.company_id} ${indoorLocationExtraDetails.location_id}"
                )
            } catch (exc: java.lang.Exception) {
                myLogger.logThis(
                    TAG, "watchIndoorLocation()", "exception ${exc.message}", exc
                )
            }
        }
    }

}