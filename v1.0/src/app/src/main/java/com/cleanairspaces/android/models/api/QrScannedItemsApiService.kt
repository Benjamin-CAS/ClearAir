package com.cleanairspaces.android.models.api

import com.cleanairspaces.android.BuildConfig
import com.cleanairspaces.android.models.api.responses.ScannedDeviceQrResponse
import com.cleanairspaces.android.utils.NONCE
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query


interface QrScannedItemsApiService {
    companion object {
        const val APP_USER = BuildConfig.APP_USER
        const val APP_USER_PWD = BuildConfig.APP_USER_PWD
        const val LOCATION_INFO_METHOD = "GetDevInfoById"
        const val LOCATION_INFO_METHOD_FOR_KEY = "GetDevInfoByIdCAS"
        const val MONITOR_INFO_METHOD_FOR_KEY = "GetDevInfoCAS"
        const val MONITOR_INFO_METHOD = "GetDevInfo"
        const val DEVICE_INFO_METHOD_FOR_KEY = "LocDataGetCAS"
        const val DEVICE_INFO_METHOD = "LocDataGet"
    }

    @POST("index.php/api/approuter")
    fun fetchScannedDeviceQrResponse(
            @Query("app_id") app_id: Int = 1,
            @Query("method") method: String,
            @Query("nonce") nonce: String = NONCE,
            @Body data: JsonObject
    ): Call<ScannedDeviceQrResponse>

    @POST("index.php/api/approuter")
    fun fetchDetailsForMyLocation(
            @Query("app_id") app_id: Int = 1,
            @Query("method") method: String,
            @Query("nonce") nonce: String = NONCE,
            @Body data: JsonObject
    ): Call<ScannedDeviceQrResponse>

}
