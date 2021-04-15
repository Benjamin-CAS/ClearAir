package com.cleanairspaces.android.models.repository

import com.cleanairspaces.android.models.api.QrScannedItemsApiService
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.DEVICE_INFO_METHOD
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.LOCATION_INFO_METHOD
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.MONITOR_INFO_METHOD
import com.cleanairspaces.android.models.api.listeners.AsyncResultListener
import com.cleanairspaces.android.models.api.responses.ScannedDeviceQrResponse
import com.cleanairspaces.android.models.dao.*
import com.cleanairspaces.android.models.entities.*
import com.cleanairspaces.android.utils.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ScannedDevicesRepo
@Inject constructor(
        private val qrScannedItemsApiService: QrScannedItemsApiService,
        private val coroutineScope: CoroutineScope,
        private val locDataFromQrDao: LocDataFromQrDao,
        private val locationDetailsDao: LocationDetailsDao,
        private val  locationHistoryThreeDaysDao : LocationHistoryThreeDaysDao,
        private val  locationHistoryWeekDao : LocationHistoryWeekDao,
        private val  locationHistoryMonthDao : LocationHistoryMonthDao,
        private val  locationHistoryUpdatesTrackerDao : LocationHistoryUpdatesTrackerDao

        ) {

    /*********** GETTERS *************/
    suspend fun getMyLocationsOnce(): List<LocationDetails> =
        locationDetailsDao.getAllMyLocationsOnce()


    private val recentlyFetchedDeviceData = arrayListOf<JsonObject>()

    fun getMyLocations(): Flow<List<LocationDetails>> = locationDetailsDao.getMyLocationsFlow()

    suspend fun getMyDeviceBy(compId: String, locId: String) = locDataFromQrDao.getMyDeviceBy(
        compId = compId,
        locId = locId
    )

    fun getADeviceFlow(compId: String, locId: String) =
        locDataFromQrDao.getADeviceFlow(companyId = compId, locationId = locId)

    fun getADeviceFlowByMonitorId(monitorId: String) =
        locDataFromQrDao.getADeviceFlow(monitorId = monitorId)


    /** history getters ***/
    fun getLastDaysHistory(deviceId  : String) = locationHistoryThreeDaysDao.getLastDaysHistory(deviceId)
    fun getLastWeekHistory(deviceId  : String) = locationHistoryWeekDao.getLastWeekHistory(deviceId)
    fun getLastMonthHistory(deviceId  : String) = locationHistoryMonthDao.getLastMonthHistory(deviceId)
    suspend fun getLastTimeUpdatedHistory(deviceId: String) = locationHistoryUpdatesTrackerDao.getLastUpdatedTimeForDevice(deviceId)

    /************ FETCH CALL BACKS ********************/
    private fun getMyLocationHistoryCallback() : Callback<ScannedDeviceQrResponse>{
        return object : Callback<ScannedDeviceQrResponse>{
            override fun onResponse(call: Call<ScannedDeviceQrResponse>, response: Response<ScannedDeviceQrResponse>) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            if (responseBody == null) {
                                MyLogger.logThis(
                                        TAG,
                                        "getMyLocationHistoryCallback() -> onResponse()",
                                        "response is OK but body is null"
                                )
                            } else {
                                if (responseBody.payload != null) {
                                    val lTime = responseBody.ltime?:"0"
                                    unEncryptHistoryPayload(
                                            responseBody.payload,
                                            lTime
                                    )
                                } else {
                                    MyLogger.logThis(
                                            TAG,
                                            "getMyLocationHistoryCallback() -> onResponse()",
                                            "response is OK but payload is null - $responseBody"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                    TAG,
                                    "getMyLocationHistoryCallback() -> onResponse()",
                                    "response is OK but an exception occurred ${e.message}",
                                    e
                            )
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                                TAG,
                                "getMyLocationHistoryCallback() -> onResponse()",
                                "response code is not 200 OK $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ScannedDeviceQrResponse>, t: Throwable) {
                MyLogger.logThis(
                        TAG,
                        "getMyLocationHistoryCallback() -> onFailure()",
                        "exc ${t.message}"
                )
            }

        }
    }

    private fun getScannedDeviceQrResponseCallback(): Callback<ScannedDeviceQrResponse> {
        return object : Callback<ScannedDeviceQrResponse> {
            override fun onResponse(
                call: Call<ScannedDeviceQrResponse>,
                response: Response<ScannedDeviceQrResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            if (responseBody == null) {
                                MyLogger.logThis(
                                    TAG,
                                    "getScannedDeviceQrResponseCallback()",
                                    "returned a null body"
                                )
                            } else {
                                if (responseBody.payload != null) {
                                    unEncryptScannedDevicePayload(
                                        responseBody.payload,
                                        responseBody.ltime
                                    )
                                } else {
                                    MyLogger.logThis(
                                        TAG, "getScannedDeviceQrResponseCallback()",
                                        "payload is null in response body $responseBody"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                TAG,
                                "getScannedDeviceQrResponseCallback()",
                                "exception ${e.message}",
                                e
                            )
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                            TAG,
                            "getScannedDeviceQrResponseCallback()",
                            "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ScannedDeviceQrResponse>, e: Throwable) {
                MyLogger.logThis(
                    TAG,
                    "getScannedDeviceQrResponseCallback()",
                    "OnFailure-exception ${e.message}"
                )
            }
        }
    }

    private fun getMyLocationResponseCallback(
        optionalResultWaiter: AsyncResultListener?,
        ignoreResultIfNotMyLocation: Boolean = false
    ): Callback<ScannedDeviceQrResponse> {
        return object : Callback<ScannedDeviceQrResponse> {
            override fun onResponse(
                call: Call<ScannedDeviceQrResponse>,
                response: Response<ScannedDeviceQrResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            if (responseBody == null || responseBody.payload.isNullOrBlank()) {
                                MyLogger.logThis(
                                    TAG,
                                    "getMyLocationResponseCallback()",
                                    "returned a null body"
                                )
                                optionalResultWaiter?.onAsyncComplete(isSuccess = false)
                            } else {
                                val lTime = responseBody.ltime?:"0"
                                unEncryptLocationDetailsPayload(
                                    payload = responseBody.payload,
                                    lTime = lTime,
                                    resultListener = optionalResultWaiter,
                                    ignoreResultIfNotMyLocation = ignoreResultIfNotMyLocation
                                )
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                TAG,
                                "getMyLocationResponseCallback()",
                                "exception ${e.message}",
                                e
                            )
                            optionalResultWaiter?.onAsyncComplete(isSuccess = false)
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                            TAG,
                            "getMyLocationResponseCallback()",
                            "response code not 200, $response"
                        )
                        optionalResultWaiter?.onAsyncComplete(isSuccess = false)
                    }
                }
            }

            override fun onFailure(call: Call<ScannedDeviceQrResponse>, e: Throwable) {
                MyLogger.logThis(
                    TAG,
                    "getMyLocationResponseCallback()",
                    "OnFailure-exception ${e.message}"
                )
                optionalResultWaiter?.onAsyncComplete(isSuccess = false)
            }
        }
    }


    /******************* fetch *************/
    fun fetchDataFromScannedDeviceQr(
        base64Str: String,
        payLoadTimeStamp: String,
        monitorId: String? = null
    ) {
        try {
            val forCompLocation = monitorId.isNullOrBlank()
            val method = if (forCompLocation) LOCATION_INFO_METHOD
            else MONITOR_INFO_METHOD
            val data = JsonObject()
            data.addProperty(L_TIME_KEY, payLoadTimeStamp)
            data.addProperty(PAYLOAD_KEY, base64Str)
            val response = qrScannedItemsApiService.fetchScannedDeviceQrResponse(
                data = data,
                method = method
            )

            // keep track of this request data ---
            data.addProperty(IS_COMP_LOC_KEY, forCompLocation)
            if (!forCompLocation) data.addProperty(MONITOR_ID_KEY, monitorId)
            recentlyFetchedDeviceData.add(data)
            response.enqueue(getScannedDeviceQrResponseCallback())
        } catch (e: Exception) {
            MyLogger.logThis(
                TAG,
                "fetchDataFromScannedDeviceQr",
                "exc ${e.message}",
                e
            )
        }
    }

    fun fetchLocationDetails(
        compId: String,
        locId: String,
        payload: String,
        lTime: String,
        userName: String,
        userPassword: String,
        getMyLocationDataListener: AsyncResultListener? = null,
        ignoreResultIfNotAlreadyMyLocation: Boolean = false,
        forScannedDeviceId : String
    ) {
        try {
            val method = DEVICE_INFO_METHOD
            val data = JsonObject()
            data.addProperty(L_TIME_KEY, lTime)
            data.addProperty(PAYLOAD_KEY, payload)
            val response = qrScannedItemsApiService.fetchDetailsForMyLocation(
                data = data,
                method = method
            )

            // keep track of this request data ---
            data.addProperty(COMP_ID_KEY, compId)
            data.addProperty(LOC_ID_KEY, locId)
            data.addProperty(USER_KEY, userName)
            data.addProperty(PASSWORD_KEY, userPassword)
            data.addProperty(DEVICE_ID_KEY, forScannedDeviceId)
            recentlyFetchedDeviceData.add(data)
            response.enqueue(
                getMyLocationResponseCallback(
                    optionalResultWaiter = getMyLocationDataListener,
                    ignoreResultIfNotMyLocation = ignoreResultIfNotAlreadyMyLocation
                )
            )
        } catch (e: Exception) {
            MyLogger.logThis(
                TAG,
                "fetchDataForMyLocation()",
                "exc ${e.message}",
                e
            )
        }
    }

    fun fetchLocationHistory(compId: String, locId: String, payload: String, lTime: String, userName: String, userPassword: String, forScannedDeviceId: String) {
        try {
            val method = DEVICE_INFO_METHOD
            val data = JsonObject()
            data.addProperty(L_TIME_KEY, lTime)
            data.addProperty(PAYLOAD_KEY, payload)
            val response = qrScannedItemsApiService.fetchLocationHistory(
                    data = data,
                    method = method
            )

            // keep track of this request data ---
            data.addProperty(COMP_ID_KEY, compId)
            data.addProperty(LOC_ID_KEY, locId)
            data.addProperty(USER_KEY, userName)
            data.addProperty(PASSWORD_KEY, userPassword)
            data.addProperty(DEVICE_ID_KEY, forScannedDeviceId)
            recentlyFetchedDeviceData.add(data)
            response.enqueue(
                    getMyLocationHistoryCallback()
            )
            MyLogger.logThis(
                    TAG,
                    "fetchLocationHistory()",
                    "data passed ${data.toString()}",
            )
        } catch (e: Exception) {
            MyLogger.logThis(
                    TAG,
                    "fetchLocationHistory()",
                    "exc ${e.message}",
                    e
            )
        }
    }



    /************ local updates **********/

    /*************** PROCESSING -- UN-ENCRYPTING --**************/

    private fun unEncryptHistoryPayload(payload: String, lTime: String) {
        try {
            val dataMatchingLTime = recentlyFetchedDeviceData.filter { it.get(L_TIME_KEY).asString.equals(lTime) }
             if (dataMatchingLTime.isNullOrEmpty()) return
             val requestedData = dataMatchingLTime[0]
                recentlyFetchedDeviceData.remove(requestedData)
             val forScannedDeviceId = requestedData.get(DEVICE_ID_KEY).asString

            val unEncryptedPayload: String =
                    QrCodeProcessor.getUnEncryptedPayloadForHistory(payload, lTime)
            val unEncJson = JSONObject(unEncryptedPayload)

            /** last 72 hours history */
            val sevenTwoHrsJsonArray = unEncJson.getJSONArray(LocationHistoryThreeDays.responseKey)
            val sevenTwoHrsArr = arrayListOf<LocationHistoryThreeDays>()
            val sevenTwoHrsTotal = sevenTwoHrsJsonArray.length()
            var i = 0
            while (i < sevenTwoHrsTotal) {
                val sevenTwoHrsData =
                        Gson().fromJson(sevenTwoHrsJsonArray.getJSONObject(i).toString(), LocationHistoryThreeDays::class.java)
                sevenTwoHrsData.forScannedDeviceId = forScannedDeviceId
                sevenTwoHrsArr.add(sevenTwoHrsData)
                i++
            }


            /** last week history */
            val weekJsonArray = unEncJson.getJSONArray(LocationHistoryWeek.responseKey)
            val weekArr = arrayListOf<LocationHistoryWeek>()
            val weekTotal = weekJsonArray.length()
            var j = 0
            while (j < weekTotal) {
                val weekData =
                        Gson().fromJson(weekJsonArray.getJSONObject(j).toString(), LocationHistoryWeek::class.java)
                weekData.forScannedDeviceId = forScannedDeviceId
                weekArr.add(weekData)
                j++
            }

            /** last month history */
            val monthJsonArray = unEncJson.getJSONArray(LocationHistoryMonth.responseKey)
            val monthArr = arrayListOf<LocationHistoryMonth>()
            val monthTotal = monthJsonArray.length()
            var k = 0
            while (k < monthTotal) {
                val monthData =
                        Gson().fromJson(monthJsonArray.getJSONObject(k).toString(), LocationHistoryMonth::class.java)
                monthData.forScannedDeviceId = forScannedDeviceId
                monthArr.add(monthData)
                k++
            }


            coroutineScope.launch(Dispatchers.IO) {

                try {
                    if (sevenTwoHrsArr.isNotEmpty()) {
                        locationHistoryThreeDaysDao.deleteAll()
                        for (data in sevenTwoHrsArr) {
                            locationHistoryThreeDaysDao.insert(data)
                        }
                    }

                    if (weekArr.isNotEmpty()) {
                        locationHistoryWeekDao.deleteAll()
                        for (data in weekArr) {
                            locationHistoryWeekDao.insert(data)
                        }
                    }

                    if (monthArr.isNotEmpty()) {
                        locationHistoryMonthDao.deleteAll()
                        for(data in monthArr) {
                            locationHistoryMonthDao.insert(data)
                        }
                    }

                    locationHistoryUpdatesTrackerDao.insertNewOrReplace(
                            LocationHistoryUpdatesTracker(forScannedDeviceId = forScannedDeviceId)
                    )
                }catch (e : java.lang.Exception){
                    MyLogger.logThis(TAG, "unEncryptHistoryPayload()" ,"saving threw an exception ${e.message}", e)
                }
            }


        }catch (e : Exception){
            MyLogger.logThis(
                    TAG,
                    "unEncryptHistoryPayload(payload - , lTime $lTime)",
                    "exception thrown ${e.message}", e
            )
        }
    }

    private fun unEncryptLocationDetailsPayload(
        payload: String,
        lTime: String,
        resultListener: AsyncResultListener? = null,
        ignoreResultIfNotMyLocation: Boolean = false
    ) {
        try {
            val dataMatchingLTime =
                recentlyFetchedDeviceData.filter { it.get(L_TIME_KEY).asString.equals(lTime) }
            if (dataMatchingLTime.isNullOrEmpty()) return
            val requestedData = dataMatchingLTime[0]

            val unEncryptedPayload: String =
                QrCodeProcessor.getUnEncryptedPayloadForLocationDetails(payload, lTime)
            val unEncJson = JSONObject(unEncryptedPayload)
            val locationDetails =
                Gson().fromJson(unEncJson.toString(), LocationDetails::class.java)

            if (locationDetails != null) {
                locationDetails.company_id = requestedData.get(COMP_ID_KEY).asString
                locationDetails.location_id = requestedData.get(LOC_ID_KEY).asString
                locationDetails.lastKnownUserName = requestedData.get(USER_KEY).asString
                locationDetails.lastKnownPassword = requestedData.get(PASSWORD_KEY).asString
                locationDetails.bound_to_scanned_device_id = requestedData.get(DEVICE_ID_KEY).asString
                locationDetails.lastUpdated = System.currentTimeMillis()

                MyLogger.logThis(
                    TAG,
                    "unEncryptLocationDetailsPayload(payload: , lTime  : $lTime)",
                    "encrypted data for company ${locationDetails.company_id} & location ${locationDetails.location_id}",
                )
                if (ignoreResultIfNotMyLocation) {
                    insertOrIgnoreLocationDetails(locationDetails, locationDetails.bound_to_scanned_device_id)
                } else {
                    insertOrOverWriteLocationDetails(locationDetails)
                }
                recentlyFetchedDeviceData.remove(requestedData)
                resultListener?.onAsyncComplete(isSuccess = true)
            } else {
                MyLogger.logThis(
                    TAG, "unEncryptLocationDetailsPayload()",
                    "failed to decode my locations details to gSon ..encoded - $unEncJson"
                )
                resultListener?.onAsyncComplete(isSuccess = false)
            }

        } catch (e: java.lang.Exception) {
            MyLogger.logThis(
                TAG,
                "unEncryptLocationDetailsPayload(payload: , lTime  : $lTime)",
                "failed ${e.message}",
                e
            )
            resultListener?.onAsyncComplete(isSuccess = false)
        }
    }


    private fun unEncryptScannedDevicePayload(payload: String, lTime: String?) {
        try {
            //identify requested data
            if (lTime == null) return
            val dataMatchingLTime =
                recentlyFetchedDeviceData.filter { it.get(L_TIME_KEY).asString.equals(lTime) }
            if (dataMatchingLTime.isNullOrEmpty()) return
            val requestedData = dataMatchingLTime[0]

            //un encrypt
            val forCompLocation = requestedData.get(IS_COMP_LOC_KEY).asBoolean
            val unEncryptedPayload =
                QrCodeProcessor.getUnEncryptedPayload(payload, lTime, forCompLocation)
            val unEncJson = JSONObject(unEncryptedPayload)
            val companyData = unEncJson.getString(LocationDataFromQr.RESPONSE_KEY)
            //if the device is a monitor
            val (type, monitor_id) = if (!forCompLocation && requestedData.has(MONITOR_ID_KEY)) {
                        Pair(
                            unEncJson.getString(LocationDataFromQr.RESPONSE_MONITOR_TYPE_KEY),
                            requestedData.get(MONITOR_ID_KEY).asString
                        )
                }else Pair("","")

            val customerDeviceData = Gson().fromJson(companyData, LocationDataFromQr::class.java)
            customerDeviceData.type = type
            customerDeviceData.monitor_id = monitor_id
            MyLogger.logThis(
                TAG,
                "processEncryptedPayload(payload: , lTime  : $lTime)",
                "success -- SAVING DATA $customerDeviceData"
            )
            insertOrUpdateCustomerDeviceData(
                customerDeviceData
            )

            //clear
            recentlyFetchedDeviceData.remove(requestedData)
        } catch (e: java.lang.Exception) {
            MyLogger.logThis(
                TAG,
                "processEncryptedPayload(payload: , lTime  : $lTime)",
                "failed ${e.message}",
                e
            )
        }
    }


    private fun insertOrOverWriteLocationDetails(locationDetails: LocationDetails) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                locationDetails.is_mine = true
                locationDetailsDao.insert(locationDetails = locationDetails)

                val (compId, locId, monitorId) = locationDetails.deConstructDeviceIdBoundedTo()
                val listLocationDataFromQr = locDataFromQrDao.getDeviceBoundToLocation(compId, locId, monitorId)
                for (locationDataFromQr in listLocationDataFromQr) {
                    locationDataFromQr.is_mine = true
                    locDataFromQrDao.updateDevice(locationDataFromQr)
                }

            } catch (e: java.lang.Exception) {
                MyLogger.logThis(
                    TAG,
                    "unEncryptLocationDetailsPayload() -- saving to room",
                    "failed ${e.message}",
                    e
                )
            }

        }
    }

    /**
     * WHEN A SCHEDULE REFRESHER RETURNS A RESULT FOR A LOCATION THAT THE USER NO LONGER
     * WISHES TO LISTEN FOR
     */
    private fun insertOrIgnoreLocationDetails(
        locationDetails: LocationDetails,
        boundToScannedDeviceId: String
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val existingLocDetails = locationDetailsDao.getDetailsForDeviceWithQr(
                    deviceId = boundToScannedDeviceId
                )
                if (existingLocDetails.isNotEmpty()) {
                    locationDetails.is_mine = true
                    locationDetailsDao.insert(locationDetails = locationDetails)
                }
            } catch (e: java.lang.Exception) {
                MyLogger.logThis(
                    TAG,
                    "unEncryptLocationDetailsPayload() -- saving to room",
                    "failed ${e.message}",
                    e
                )
            }

        }
    }

    private fun insertOrUpdateCustomerDeviceData(
        customerDeviceData: LocationDataFromQr
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            //check if user has marked this as their location
            val isFound = locDataFromQrDao.checkIfIsMyLocation(
                compId = customerDeviceData.company_id,
                locId = customerDeviceData.location_id,
                monitorId = customerDeviceData.monitor_id
            )
            customerDeviceData.is_mine = (isFound > 0)
            locDataFromQrDao.insert(customerDeviceData)
        }
    }

    suspend fun removeMyDevice(deviceData: LocationDataFromQr) {
        deviceData.is_mine = false
        val forScannedDeviceId = createDeviceIdToBindTo(compId = deviceData.company_id, locId = deviceData.location_id, monitorId = deviceData.monitor_id)
        locDataFromQrDao.updateDevice(deviceData)
        val locationsDetails = locationDetailsDao.getDetailsForDeviceWithQr(deviceId = forScannedDeviceId)
        if (locationsDetails.isNotEmpty()){
            locationDetailsDao.deleteAll(locationsDetails)
        }
        MyLogger.logThis(TAG, "removing devices", "${locationsDetails.size}  $forScannedDeviceId")
    }


    companion object {
        private const val MONITOR_ID_KEY = "monitor_id"
        private const val IS_COMP_LOC_KEY = "forCompLocation"
        private const val COMP_ID_KEY = "compId"
        private const val LOC_ID_KEY = "locId"
        private val TAG = ScannedDevicesRepo::class.java.simpleName
        private const val DEVICE_ID_KEY = "device_id"
    }
}