package com.android_dev.cleanairspaces.persistence.api.services

import com.android_dev.cleanairspaces.persistence.api.responses.*
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.CONTROL_DEVICE_METHOD
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.DEVICE_METHOD
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.GET_AC_DEVICES_BY_LOC_ID
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.INDOOR_LOCATION_DETAILS_METHOD
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.INDOOR_LOCATION_MONITORS_METHOD
import com.android_dev.cleanairspaces.utils.API_APP_ID
import com.android_dev.cleanairspaces.utils.NONCE
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface InDoorLocationApiService {
    companion object {
        const val INDOOR_LOCATIONS_INFO_METHOD = "ComAll"
    }

    @POST("index.php/api/router")
    fun fetchInDoorLocations(
        @Query("app_id") app_id: Int = API_APP_ID,
        @Query("method") method: String = INDOOR_LOCATIONS_INFO_METHOD,
        @Query("nonce") nonce: String = NONCE,
        @Body pl: String
    ): Call<IndoorLocationsResponse>


    @POST("index.php/api/approuter")
    fun fetchInDoorLocationsDetails(
        @Query("app_id") app_id: Int = API_APP_ID,
        @Query("method") method: String = INDOOR_LOCATION_DETAILS_METHOD,
        @Query("nonce") nonce: String = NONCE,
        @Body pl: JsonObject
    ): Call<IndoorLocationsDetailsResponse>

    @POST("index.php/api/approuter")
    fun fetchInDoorLocationsMonitors(
        @Query("app_id") app_id: Int = API_APP_ID,
        @Query("method") method: String = INDOOR_LOCATION_MONITORS_METHOD,
        @Query("nonce") nonce: String = NONCE,
        @Body pl: JsonObject
    ): Call<IndoorMonitorsResponse>

    @POST("index.php/api/approuter")
    fun fetchInDoorLocationsDevices(
        @Query("app_id") app_id: Int = API_APP_ID,
        @Query("method") method: String = DEVICE_METHOD,
        @Query("nonce") nonce: String = NONCE,
        @Body pl: JsonObject
    ): Call<DevicesDetailsResponse>

    @POST("index.php/api/approuter")
    fun updateDeviceStatusByHttp(
        @Query("app_id") app_id: Int = API_APP_ID,
        @Query("method") method: String = CONTROL_DEVICE_METHOD,
        @Query("nonce") nonce: String = NONCE,
        @Body pl: JsonObject
    ): Call<Any>
    @GET("index.php/api/cmsapi")
    suspend fun getAirConditionerDevices(
        @Query("app_id") app_id: Int = API_APP_ID,
        @Query("method") method: String = GET_AC_DEVICES_BY_LOC_ID,
        @Query("nonce") nonce: String = NONCE,
        @Query("l") l:String = "1931"
    ): GetAirConditionerRes
}