package com.cleanairspaces.android.models.repository

import com.cleanairspaces.android.models.api.QrScannedItemsApiService
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
    private val customerDeviceDataDao : CustomerDeviceDataDao
        ) {

    private val TAG = ScannedDevicesRepo::class.java.simpleName

    /************************ QR SCANNED ITEMS *********************/
    fun getADeviceFlow(compId : String, locId : String) = customerDeviceDataDao.getADeviceFlow(companyId = compId, locationId = locId)

    private fun getScannedDeviceQrResponseCallback() : Callback<ScannedDeviceQrResponse> {
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
                                MyLogger.logThis(
                                    TAG, "getScannedDeviceQrResponseCallback()",
                                    " in response body ${responseBody.payload}"
                                )
                                processLocationPayload(responseBody.payload, responseBody.ltime)
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

    private fun processLocationPayload(payload: String, lTime  : String) {
        try {
            val unEncyptedPayload = QrCodeProcessor.getUnEncryptedPayload(payload, lTime)
            val companyData= JSONObject(unEncyptedPayload).getString(CustomerDeviceData.RESPONSE_KEY)
            val customerDeviceData = Gson().fromJson(companyData, CustomerDeviceData::class.java)
            MyLogger.logThis(TAG,
                "processEncryptedPayload(payload: $payload, lTime  : $lTime)",
                "success -- SAVING DATA $customerDeviceData")
            coroutineScope.launch(Dispatchers.IO) {
                val isFound = customerDeviceDataDao.checkIfIsMyLocation(companyId = customerDeviceData.company_id, locationId = customerDeviceData.location_id)
                customerDeviceData.isMyDeviceData = (isFound > 0)
                customerDeviceDataDao.insertDeviceData(customerDeviceData)
            }
        }catch (e : java.lang.Exception){
            MyLogger.logThis(TAG, "processEncryptedPayload(payload: $payload, lTime  : $lTime)"
            , "failed ${e.message}", e)
        }
    }


    suspend fun fetchLocationFromScannedDeviceQr(base64Str: String, payLoadTimeStamp: String) {
        try {
            val data = JsonObject()
            data.addProperty(L_TIME_KEY, payLoadTimeStamp)
            data.addProperty(PAYLOAD_KEY, base64Str)
            val otherLocationsResponse = qrScannedItemsApiService.fetchScannedDeviceQrResponse(
                data = data
            )

            MyLogger.logThis(TAG, "fetchLocationFromScannedDeviceQr()", "passing data $data")
            otherLocationsResponse.enqueue(getScannedDeviceQrResponseCallback())
        }catch (e : Exception){
            MyLogger.logThis(TAG, "fetchLocationFromScannedDeviceQr()", "exc ${e.message}", e)
        }
    }

   suspend fun addMyLocationData(customerDeviceData: CustomerDeviceData, userName: String?, userPassword: String?) {
        if (userName != null && userPassword != null && customerDeviceData.isSecure){
            //todo validate credentials
        }else{
            customerDeviceData.isMyDeviceData = true
            customerDeviceDataDao.updateDevice(customerDeviceData)
        }
    }
}