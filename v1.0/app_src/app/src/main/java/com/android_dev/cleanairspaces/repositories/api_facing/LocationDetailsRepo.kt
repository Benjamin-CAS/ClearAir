package com.android_dev.cleanairspaces.repositories.api_facing

import androidx.lifecycle.MutableLiveData
import com.android_dev.cleanairspaces.persistence.api.responses.LocationDataFromQr
import com.android_dev.cleanairspaces.persistence.api.responses.ScannedDeviceQrWithCompLocResponse
import com.android_dev.cleanairspaces.persistence.api.services.QrScannedItemsApiService
import com.android_dev.cleanairspaces.persistence.local.models.dao.WatchedLocationHighLightsDao
import com.android_dev.cleanairspaces.utils.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationDetailsRepo
@Inject constructor(
        private val coroutineScope: CoroutineScope,
        private val watchedLocationHighLightsDao: WatchedLocationHighLightsDao,
        private val qrScannedItemsApiService: QrScannedItemsApiService
) {

    private val TAG = LocationDetailsRepo::class.java.simpleName
    private val recentRequestsData = arrayListOf<JsonObject>()

    private lateinit var currentlyScannedDeviceData: MutableLiveData<LocationDataFromQr>
    fun getCurrentlyScannedDeviceData() = currentlyScannedDeviceData
    fun initCurrentlyScannedDeviceData() {
        currentlyScannedDeviceData = MutableLiveData<LocationDataFromQr>()
    }

    /************* SAVE ***********/
    suspend fun fetchDataForScannedDeviceWithCompLoc(base64Str: String, payLoadTimeStamp: String) {
        try {
            val data = JsonObject()
            data.addProperty(L_TIME_KEY, payLoadTimeStamp)
            data.addProperty(PAYLOAD_KEY, base64Str)
            val response = qrScannedItemsApiService.fetchDataForScannedDeviceWithCompLoc(
                    data = data
            )

            // keep track of this request data ---
            recentRequestsData.add(data)
            response.enqueue(getScannedDeviceQrResponseCallback(hasCompLocId = true))
        } catch (e: Exception) {
            MyLogger.logThis(
                    TAG,
                    "fetchDataForScannedDeviceWithCompLoc",
                    "exc ${e.message}",
                    e
            )
        }
    }


    fun fetchDataForScannedDeviceWithMonitorId(base64Str: String, payLoadTimeStamp: String, monitorId: String) {
        try {
            val data = JsonObject()
            data.addProperty(L_TIME_KEY, payLoadTimeStamp)
            data.addProperty(PAYLOAD_KEY, base64Str)
            val response = qrScannedItemsApiService.fetchDataForScannedDeviceWithMonitorId(
                    data = data
            )

            // keep track of this request data ---
            data.addProperty(MONITOR_ID_KEY, monitorId)
            recentRequestsData.add(data)
            response.enqueue(getScannedDeviceQrResponseCallback(hasMonitorId = true))
        } catch (e: Exception) {
            MyLogger.logThis(
                    TAG,
                    "fetchDataForScannedDeviceWithMonitorId",
                    "exc ${e.message}",
                    e
            )
        }
    }


    /************** call backs *************/
    private fun getScannedDeviceQrResponseCallback(
            hasMonitorId: Boolean = false,
            hasCompLocId: Boolean = false
    ): Callback<ScannedDeviceQrWithCompLocResponse> {
        return object : Callback<ScannedDeviceQrWithCompLocResponse> {
            override fun onResponse(
                    call: Call<ScannedDeviceQrWithCompLocResponse>,
                    response: Response<ScannedDeviceQrWithCompLocResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            if (responseBody == null) {
                                MyLogger.logThis(
                                        TAG,
                                        "getScannedDeviceQrResponseCallback",
                                        "returned a null body"
                                )
                            } else {
                                if (responseBody.payload != null) {
                                    when {
                                        hasCompLocId -> {
                                            unEncryptScannedDeviceQrWithCompLocResponse(
                                                    responseBody.payload,
                                                    responseBody.ltime ?: "0"
                                            )
                                        }
                                        hasMonitorId -> {
                                            unEncryptScannedDeviceQrWithMonitorIdResponse(
                                                    responseBody.payload,
                                                    responseBody.ltime ?: "0"
                                            )
                                        }
                                        else -> {
                                            MyLogger.logThis(
                                                    TAG, "getScannedDeviceQrResponseCallback",
                                                    "response body is $responseBody - unknown device"
                                            )
                                        }
                                    }
                                } else {
                                    MyLogger.logThis(
                                            TAG, "getScannedDeviceQrResponseCallback",
                                            "payload is null in response body $responseBody"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                    TAG,
                                    "getScannedDeviceQrResponseCallback",
                                    "exception ${e.message}",
                                    e
                            )
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                                TAG,
                                "getScannedDeviceQrResponseCallback",
                                "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ScannedDeviceQrWithCompLocResponse>, e: Throwable) {
                MyLogger.logThis(
                        TAG,
                        "getScannedDeviceQrResponseCallback",
                        "OnFailure-exception ${e.message}"
                )
            }
        }
    }

    private fun unEncryptScannedDeviceQrWithCompLocResponse(payload: String, lTime: String) {
        try {
            //identify requested data
            val dataMatchingLTime =
                    recentRequestsData.filter { it.get(L_TIME_KEY).asString.equals(lTime) }
            if (dataMatchingLTime.isNullOrEmpty()) return
            val requestedData = dataMatchingLTime[0]
            recentRequestsData.remove(requestedData)
            //un encrypt
            val unEncryptedPayload =
                    CasEncDecQrProcessor.decodeApiResponse(payload)
            val unEncJson = JSONObject(unEncryptedPayload)

            val companyData = unEncJson.getString(ScannedDeviceQrWithCompLocResponse.response_key)
            val customerDeviceData = Gson().fromJson(companyData, LocationDataFromQr::class.java)

            MyLogger.logThis(
                    TAG, "unEncryptScannedDeviceQrWithCompLocResponse",
                    "unEncJson $unEncJson customerData $customerDeviceData"
            )
            currentlyScannedDeviceData.value = customerDeviceData!!
        } catch (e: java.lang.Exception) {
            MyLogger.logThis(
                    TAG, "unEncryptScannedDeviceQrWithCompLocResponse -pl $payload",
                    "failed ${e.message}",
                    e
            )
        }
    }

    private fun unEncryptScannedDeviceQrWithMonitorIdResponse(payload: String, lTime: String) {
        try {
            //identify requested data
            val dataMatchingLTime =
                    recentRequestsData.filter { it.get(L_TIME_KEY).asString.equals(lTime) }
            if (dataMatchingLTime.isNullOrEmpty()) return
            val requestedData = dataMatchingLTime[0]
            recentRequestsData.remove(requestedData)
            //un encrypt
            val unEncryptedPayload =
                    CasEncDecQrProcessor.decodeApiResponse(payload)
            val unEncJson = JSONObject(unEncryptedPayload)

            val companyData = unEncJson.getString(ScannedDeviceQrWithCompLocResponse.response_key)
            val customerDeviceData = Gson().fromJson(companyData, LocationDataFromQr::class.java)
            val monitorId = requestedData.get(MONITOR_ID_KEY).asString

            val type = unEncJson.getString(LocationDataFromQr.RESPONSE_MONITOR_TYPE_KEY)
            customerDeviceData.monitor_id = monitorId
            customerDeviceData.type = type

            MyLogger.logThis(
                    TAG, "unEncryptScannedDeviceQrWithMonitorIdResponse",
                    "unEncJson $unEncJson customerData $customerDeviceData"
            )
            currentlyScannedDeviceData.value = customerDeviceData!!
        } catch (e: java.lang.Exception) {
            MyLogger.logThis(
                    TAG, "unEncryptScannedDeviceQrWithMonitorIdResponse -pl $payload",
                    "failed ${e.message}",
                    e
            )
        }
    }

}