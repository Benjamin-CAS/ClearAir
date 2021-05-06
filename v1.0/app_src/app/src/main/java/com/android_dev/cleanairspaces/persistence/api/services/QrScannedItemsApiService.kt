package com.android_dev.cleanairspaces.persistence.api.services

import com.android_dev.cleanairspaces.persistence.api.responses.ScannedDeviceQrWithCompLocResponse
import com.android_dev.cleanairspaces.utils.API_APP_ID
import com.android_dev.cleanairspaces.utils.NONCE
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface QrScannedItemsApiService {

    @POST("index.php/api/approuter")
    fun fetchDataForScannedDeviceWithCompLoc(
        @Query("app_id") app_id: Int = API_APP_ID,
        @Query("method") method: String = AppApiService.LOCATION_INFO_METHOD,
        @Query("nonce") nonce: String = NONCE,
        @Body data: JsonObject
    ): Call<ScannedDeviceQrWithCompLocResponse>

    @POST("index.php/api/approuter")
    fun fetchDataForScannedDeviceWithMonitorId(
        @Query("app_id") app_id: Int = API_APP_ID,
        @Query("method") method: String = AppApiService.MONITOR_INFO_METHOD,
        @Query("nonce") nonce: String = NONCE,
        @Body data: JsonObject
    ): Call<ScannedDeviceQrWithCompLocResponse>

}