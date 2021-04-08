package com.cleanairspaces.android.models.repository

import com.cleanairspaces.android.models.api.QrScannedItemsApiService
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.DEVICE_INFO_METHOD
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.LOCATION_INFO_METHOD
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.MONITOR_INFO_METHOD
import com.cleanairspaces.android.models.api.listeners.AsyncResultListener
import com.cleanairspaces.android.models.api.responses.ScannedDeviceQrResponse
import com.cleanairspaces.android.models.dao.CustomerDeviceDataDao
import com.cleanairspaces.android.models.dao.MyLocationDetailsDao
import com.cleanairspaces.android.models.entities.CustomerDeviceData
import com.cleanairspaces.android.models.entities.CustomerDeviceDataDetailed
import com.cleanairspaces.android.models.entities.MyLocationDetails
import com.cleanairspaces.android.utils.L_TIME_KEY
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.PAYLOAD_KEY
import com.cleanairspaces.android.utils.QrCodeProcessor
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
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
    private val customerDeviceDataDao: CustomerDeviceDataDao,
    private val myLocationDetailsDao: MyLocationDetailsDao
) {

    private val recentlyFetchedDeviceData = arrayListOf<JsonObject>()


    private val myLocationDetailsLive = MutableStateFlow<List<CustomerDeviceDataDetailed>>(
        arrayListOf()
    )

    fun getMyLocations(): Flow<List<CustomerDeviceDataDetailed>> {
        return myLocationDetailsDao.getMyLocationsFlow().flatMapLatest {
            val myLocationDetails = arrayListOf<CustomerDeviceDataDetailed>()
            if (it.isNotEmpty()) {
                for (locationDetails in it) {
                    val foundCustomerDeviceData = customerDeviceDataDao.getDeviceBy(
                        compId = locationDetails.company_id,
                        locId = locationDetails.location_id
                    )
                    myLocationDetails.add(
                        CustomerDeviceDataDetailed(
                            locationDetails = locationDetails,
                            deviceData = foundCustomerDeviceData[0]
                        )
                    )
                }
                myLocationDetailsLive.value = myLocationDetails
            }
            myLocationDetailsLive
        }
    }

    /************************ QR SCANNED ITEMS *********************/
    fun getADeviceFlow(compId: String, locId: String) =
        customerDeviceDataDao.getADeviceFlow(companyId = compId, locationId = locId)

    fun getADeviceFlowByMonitorId(monitorId: String) =
        customerDeviceDataDao.getADeviceFlowByMonitorId(monitorId = monitorId)

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
                                    MyLogger.logThis(
                                        TAG, "getScannedDeviceQrResponseCallback()",
                                        " in response body ${responseBody.payload}"
                                    )
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

    private fun getMyLocationResponseCallback(getMyLocationDataListener: AsyncResultListener): Callback<ScannedDeviceQrResponse> {
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
                                getMyLocationDataListener.onAsyncComplete(isSuccess = false)
                            } else {
                                MyLogger.logThis(
                                    TAG, "getMyLocationResponseCallback()",
                                    "payload is null in response body $responseBody"
                                )
                                unEncryptLocationDetailsPayload(
                                    payload = responseBody.payload,
                                    lTime = responseBody.ltime,
                                    resultListener = getMyLocationDataListener
                                )
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                TAG,
                                "getMyLocationResponseCallback()",
                                "exception ${e.message}",
                                e
                            )
                            getMyLocationDataListener.onAsyncComplete(isSuccess = false)
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                            TAG,
                            "getMyLocationResponseCallback()",
                            "response code not 200, $response"
                        )
                        getMyLocationDataListener.onAsyncComplete(isSuccess = false)
                    }
                }
            }

            override fun onFailure(call: Call<ScannedDeviceQrResponse>, e: Throwable) {
                MyLogger.logThis(
                    TAG,
                    "getMyLocationResponseCallback()",
                    "OnFailure-exception ${e.message}"
                )
                getMyLocationDataListener.onAsyncComplete(isSuccess = false)
            }
        }
    }


    /*************** PROCESSING -- UN-ENCRYPTING --**************/
    private fun unEncryptLocationDetailsPayload(
        payload: String,
        lTime: String,
        resultListener: AsyncResultListener
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
                Gson().fromJson(unEncJson.toString(), MyLocationDetails::class.java)
            locationDetails.company_id = requestedData.get(COMP_ID_KEY).asString
            locationDetails.location_id = requestedData.get(LOC_ID_KEY).asString
            locationDetails.lastUpdated = lTime.toLong()

            MyLogger.logThis(
                TAG,
                "unEncryptLocationDetailsPayload(payload: $payload, lTime  : $lTime)",
                "encrypted data for company ${locationDetails.company_id} & location ${locationDetails.location_id}",
            )
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    myLocationDetailsDao.insertMyLocationDetails(myLocationDetails = locationDetails)
                    customerDeviceDataDao.updateIsMyLocation(
                        compId = locationDetails.company_id,
                        locId = locationDetails.location_id
                    )
                } catch (e: java.lang.Exception) {
                    MyLogger.logThis(
                        TAG,
                        "unEncryptLocationDetailsPayload() -- saving to room",
                        "failed ${e.message}",
                        e
                    )
                }

            }
            resultListener.onAsyncComplete(isSuccess = true)

        } catch (e: java.lang.Exception) {
            MyLogger.logThis(
                TAG,
                "unEncryptLocationDetailsPayload(payload: $payload, lTime  : $lTime)",
                "failed ${e.message}",
                e
            )
            resultListener.onAsyncComplete(isSuccess = false)
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
            val companyData = unEncJson.getString(CustomerDeviceData.RESPONSE_KEY)
            val type =
                if (!forCompLocation) unEncJson.getString(CustomerDeviceData.RESPONSE_MONITOR_TYPE_KEY)
                else ""
            val customerDeviceData = Gson().fromJson(companyData, CustomerDeviceData::class.java)
            coroutineScope.launch(Dispatchers.IO) {
                val isFound = customerDeviceDataDao.checkIfIsMyLocation(
                    companyId = customerDeviceData.company_id,
                    locationId = customerDeviceData.location_id
                )
                customerDeviceData.isMyDeviceData = (isFound > 0)
                if (!forCompLocation && requestedData.has(MONITOR_ID_KEY)) {
                    val forMonitorId = requestedData.get(MONITOR_ID_KEY).asString
                    customerDeviceData.monitor_id = forMonitorId
                    customerDeviceData.type = type
                }
                MyLogger.logThis(
                    TAG,
                    "processEncryptedPayload(payload: $payload, lTime  : $lTime)",
                    "success -- SAVING DATA $customerDeviceData"
                )

                customerDeviceDataDao.insertDeviceData(customerDeviceData)
                //clear
                recentlyFetchedDeviceData.remove(requestedData)
            }
        } catch (e: java.lang.Exception) {
            MyLogger.logThis(
                TAG,
                "processEncryptedPayload(payload: $payload, lTime  : $lTime)",
                "failed ${e.message}",
                e
            )
        }
    }


    /******************* fetch *************/
    fun fetchDataFromScannedDeviceQr(
        base64Str: String,
        payLoadTimeStamp: String,
        forCompLocation: Boolean,
        monitorId: String? = null
    ) {
        try {
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
            if (monitorId != null) data.addProperty(MONITOR_ID_KEY, monitorId)
            recentlyFetchedDeviceData.add(data)

            response.enqueue(getScannedDeviceQrResponseCallback())
        } catch (e: Exception) {
            MyLogger.logThis(
                TAG,
                "fetchLocationFromScannedDeviceQr()",
                "exc ${e.message}",
                e
            )
        }
    }

    fun fetchDataForMyLocation(
        compId: String,
        locId: String,
        payload: String,
        ltime: String,
        getMyLocationDataListener: AsyncResultListener
    ) {
        try {
            val method = DEVICE_INFO_METHOD
            val data = JsonObject()
            data.addProperty(L_TIME_KEY, ltime)
            data.addProperty(PAYLOAD_KEY, payload)
            val response = qrScannedItemsApiService.fetchDetailsForMyLocation(
                data = data,
                method = method
            )

            // keep track of this request data ---
            data.addProperty(COMP_ID_KEY, compId)
            data.addProperty(LOC_ID_KEY, locId)
            recentlyFetchedDeviceData.add(data)
            MyLogger.logThis(TAG, "fetchDataForMyLocation()", "passing data $data")
            response.enqueue(getMyLocationResponseCallback(getMyLocationDataListener = getMyLocationDataListener))
        } catch (e: Exception) {
            MyLogger.logThis(
                TAG,
                "fetchDataForMyLocation()",
                "exc ${e.message}",
                e
            )
        }
    }


    /************ updates **********/
    suspend fun updateLocationIsMineStatus(
        customerDeviceData: CustomerDeviceData,
        isMine: Boolean
    ) {
        customerDeviceData.isMyDeviceData = isMine
        customerDeviceDataDao.updateDevice(customerDeviceData)
    }


    companion object {
        private const val MONITOR_ID_KEY = "monitor_id"
        private const val IS_COMP_LOC_KEY = "forCompLocation"
        private const val COMP_ID_KEY = "compId"
        private const val LOC_ID_KEY = "locId"
        private val TAG = ScannedDevicesRepo::class.java.simpleName
    }
}