package com.android_dev.cleanairspaces.persistence.api.services

import com.android_dev.cleanairspaces.persistence.api.responses.OutDoorDetailsLocationResponse
import com.android_dev.cleanairspaces.persistence.api.responses.OutDoorLocationAmerica
import com.android_dev.cleanairspaces.persistence.api.responses.OutDoorLocationResponse
import com.android_dev.cleanairspaces.persistence.api.responses.OutDoorLocationTaiwan
import com.android_dev.cleanairspaces.utils.API_APP_ID
import com.android_dev.cleanairspaces.utils.NONCE
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OutDoorLocationApiService {
    companion object {
        const val OUTDOOR_LOCATIONS_INFO_METHOD = "OutLocInfo"
        const val OUTDOOR_LOCATIONS_EXTRA_INFO_METHOD  = "GetOutLoc"
    }

    @POST("index.php/api/router")
    fun fetchOutDoorLocationsExtraDetails(
            @Query("app_id") app_id: Int = API_APP_ID,
            @Query("method") method: String = OUTDOOR_LOCATIONS_EXTRA_INFO_METHOD,
            @Query("nonce") nonce: String = NONCE,
            @Query("user") user: String = AppApiService.APP_USER,
            @Query("pwd") pwd: String = AppApiService.APP_USER_PWD,
            @Body data: JsonObject
    ): Call<OutDoorDetailsLocationResponse>

    @GET("index.php/api/router")
    fun fetchOtherOutDoorLocations(
            @Query("app_id") app_id: Int = API_APP_ID,
            @Query("method") method: String = OUTDOOR_LOCATIONS_INFO_METHOD,
            @Query("nonce") nonce: String = NONCE,
            @Query("user") user: String = AppApiService.APP_USER,
            @Query("pwd") pwd: String = AppApiService.APP_USER_PWD
    ): Call<OutDoorLocationResponse>

    @GET("us_resp.php")
    fun fetchAmericanOutDoorLocations(
    ): Call<List<OutDoorLocationAmerica>>

    @GET("tw_resp.php")
    fun fetchTaiwanOutDoorLocations(
    ): Call<List<OutDoorLocationTaiwan>>

}
