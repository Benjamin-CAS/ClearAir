package com.android_dev.cleanairspaces.repositories.api_facing

import androidx.lifecycle.MutableLiveData
import com.android_dev.cleanairspaces.persistence.api.responses.LocationDataFromQr
import com.android_dev.cleanairspaces.persistence.api.responses.ScannedDeviceQrWithCompLocResponse
import com.android_dev.cleanairspaces.persistence.api.services.QrScannedItemsApiService
import com.android_dev.cleanairspaces.utils.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationDetailsRepo
@Inject constructor(
    private val qrScannedItemsApiService: QrScannedItemsApiService,
    private val myLogger: MyLogger,

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
        } catch (exc: Exception) {
            myLogger.logThis(
                tag = LogTags.EXCEPTION,
                from = "$TAG fetchDataForScannedDeviceWithCompLoc()",
                msg = exc.message,
                exc = exc
            )

        }
    }


    suspend fun fetchDataForScannedDeviceWithMonitorId(
        base64Str: String,
        payLoadTimeStamp: String,
        monitorId: String
    ) {
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
        } catch (exc: Exception) {
            myLogger.logThis(
                tag = LogTags.EXCEPTION,
                from = "$TAG fetchDataForScannedDeviceWithMonitorId()",
                msg = exc.message,
                exc = exc
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
                            if (responseBody?.payload != null) {
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
                                    }
                                }
                            }
                        } catch (exc: Exception) {
                            CoroutineScope(Dispatchers.IO).launch {
                                myLogger.logThis(
                                    tag = LogTags.EXCEPTION,
                                    from = "$TAG getScannedDeviceQrResponseCallback()",
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

            override fun onFailure(call: Call<ScannedDeviceQrWithCompLocResponse>, e: Throwable) {
            }
        }
    }

    private fun unEncryptScannedDeviceQrWithCompLocResponse(payload: String, lTime: String) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                //identify requested data
                val dataMatchingLTime =
                    recentRequestsData.filter { it.get(L_TIME_KEY).asString.equals(lTime) }
                if (!dataMatchingLTime.isNullOrEmpty()) {
                    val requestedData = dataMatchingLTime[0]
                    recentRequestsData.remove(requestedData)
                    //un encrypt
                    val unEncryptedPayload =
                        CasEncDecQrProcessor.decodeApiResponse(payload)
                    val unEncJson = JSONObject(unEncryptedPayload)

                    val companyData =
                        unEncJson.getString(ScannedDeviceQrWithCompLocResponse.response_key)
                    val customerDeviceData =
                        Gson().fromJson(companyData, LocationDataFromQr::class.java)

                    withContext(Dispatchers.Main) {
                        currentlyScannedDeviceData.value = customerDeviceData!!
                    }
                }
            } catch (exc: Exception) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG unEncryptScannedDeviceQrWithCompLocResponse()",
                    msg = exc.message,
                    exc = exc
                )

            }
        }

    }

    private fun unEncryptScannedDeviceQrWithMonitorIdResponse(payload: String, lTime: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //identify requested data
                val dataMatchingLTime =
                    recentRequestsData.filter { it.get(L_TIME_KEY).asString.equals(lTime) }
                if (!dataMatchingLTime.isNullOrEmpty()) {
                    val requestedData = dataMatchingLTime[0]
                    recentRequestsData.remove(requestedData)
                    //un encrypt
                    val unEncryptedPayload =
                        CasEncDecQrProcessor.decodeApiResponse(payload)
                    val unEncJson = JSONObject(unEncryptedPayload)

                    val companyData =
                        unEncJson.getString(ScannedDeviceQrWithCompLocResponse.response_key)
                    val customerDeviceData =
                        Gson().fromJson(companyData, LocationDataFromQr::class.java)
                    val monitorId = requestedData.get(MONITOR_ID_KEY).asString

                    val type = unEncJson.getString(LocationDataFromQr.RESPONSE_MONITOR_TYPE_KEY)
                    customerDeviceData.monitor_id = monitorId
                    customerDeviceData.type = type

                    withContext(Dispatchers.Main) {
                        currentlyScannedDeviceData.value = customerDeviceData!!
                    }
                }
            } catch (exc: Exception) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG unEncryptScannedDeviceQrWithMonitorIdResponse()",
                    msg = exc.message,
                    exc = exc
                )

            }
        }
    }

}