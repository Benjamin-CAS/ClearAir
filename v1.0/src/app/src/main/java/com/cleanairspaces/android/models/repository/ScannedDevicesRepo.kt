package com.cleanairspaces.android.models.repository

import com.cleanairspaces.android.models.api.QrScannedItemsApiService
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.LOCATION_INFO_METHOD
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.MONITOR_INFO_METHOD
import com.cleanairspaces.android.models.api.responses.ScannedDeviceQrResponse
import com.cleanairspaces.android.models.dao.CustomerDeviceDataDao
import com.cleanairspaces.android.models.entities.CustomerDeviceData
import com.cleanairspaces.android.utils.L_TIME_KEY
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.PAYLOAD_KEY
import com.cleanairspaces.android.utils.QrCodeProcessor
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val customerDeviceDataDao: CustomerDeviceDataDao
) {

    private lateinit var recentlyFetchedQrCodeData: JsonObject

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
                                    Companion.TAG,
                                    "getScannedDeviceQrResponseCallback()",
                                    "returned a null body"
                                )
                            } else {
                                if (responseBody.payload != null) {
                                    MyLogger.logThis(
                                        Companion.TAG, "getScannedDeviceQrResponseCallback()",
                                        " in response body ${responseBody.payload}"
                                    )
                                    processReturnedEncPayLoad(
                                        responseBody.payload,
                                        responseBody.ltime
                                    )
                                } else {
                                    MyLogger.logThis(
                                        Companion.TAG, "getScannedDeviceQrResponseCallback()",
                                        "payload is null in response body $responseBody"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                Companion.TAG,
                                "getScannedDeviceQrResponseCallback()",
                                "exception ${e.message}",
                                e
                            )
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                            Companion.TAG,
                            "getScannedDeviceQrResponseCallback()",
                            "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ScannedDeviceQrResponse>, e: Throwable) {
                MyLogger.logThis(
                    Companion.TAG,
                    "getScannedDeviceQrResponseCallback()",
                    "OnFailure-exception ${e.message}"
                )
            }
        }
    }

    private fun processReturnedEncPayLoad(payload: String, timeStmp: String?) {
        try {
            var lTime = timeStmp
            var forCompLocation = true
            if (::recentlyFetchedQrCodeData.isInitialized) {
                //todo may not be necessary if response identifies this
                if (timeStmp == null) {
                    lTime = recentlyFetchedQrCodeData.get(L_TIME_KEY).asString
                }
                forCompLocation = recentlyFetchedQrCodeData.get(IS_COMP_LOC_KEY).asBoolean
            }
            if (lTime == null) return
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
                if (!forCompLocation && recentlyFetchedQrCodeData.has(MONITOR_ID_KEY)) {
                    //todo match with timestamp returned
                    val forMonitorId = recentlyFetchedQrCodeData.get(MONITOR_ID_KEY).asString
                    customerDeviceData.monitor_id = forMonitorId
                    customerDeviceData.type = type
                }
                MyLogger.logThis(
                    Companion.TAG,
                    "processEncryptedPayload(payload: $payload, lTime  : $lTime)",
                    "success -- SAVING DATA $customerDeviceData"
                )

                customerDeviceDataDao.insertDeviceData(customerDeviceData)
            }
        } catch (e: java.lang.Exception) {
            MyLogger.logThis(
                Companion.TAG,
                "processEncryptedPayload(payload: $payload, lTime  : $timeStmp)",
                "failed ${e.message}",
                e
            )
        }
    }


    suspend fun fetchDataFromScannedDeviceQr(
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
            val otherLocationsResponse = qrScannedItemsApiService.fetchScannedDeviceQrResponse(
                data = data,
                method = method
            )
            MyLogger.logThis(TAG, "fetchLocationFromScannedDeviceQr()", "passing data $data")
            //todo? may not be needed if response identifies this
            //if timestamp is returned you can match it with @payLoadTimeStamp
            data.addProperty(IS_COMP_LOC_KEY, forCompLocation)
            if (monitorId != null) data.addProperty(MONITOR_ID_KEY, monitorId)
            recentlyFetchedQrCodeData = data
            otherLocationsResponse.enqueue(getScannedDeviceQrResponseCallback())
        } catch (e: Exception) {
            MyLogger.logThis(
                Companion.TAG,
                "fetchLocationFromScannedDeviceQr()",
                "exc ${e.message}",
                e
            )
        }
    }

    suspend fun addMyLocationData(
        customerDeviceData: CustomerDeviceData,
        userName: String?,
        userPassword: String?
    ) {
        if (userName != null && userPassword != null && customerDeviceData.isSecure) {
            //todo validate credentials
        } else {
            customerDeviceData.isMyDeviceData = true
            customerDeviceDataDao.updateDevice(customerDeviceData)
        }
    }

    companion object {
        private const val MONITOR_ID_KEY = "monitor_id"
        private const val IS_COMP_LOC_KEY = "forCompLocation"
        private val TAG = ScannedDevicesRepo::class.java.simpleName
    }
}