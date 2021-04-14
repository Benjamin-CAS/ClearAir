package com.cleanairspaces.android.models.api

import com.cleanairspaces.android.BuildConfig
import com.cleanairspaces.android.models.api.responses.OutDoorLocationAmerica
import com.cleanairspaces.android.models.api.responses.OutDoorLocationTaiwan
import com.cleanairspaces.android.models.api.responses.OutDoorLocationsOtherResponse
import com.cleanairspaces.android.utils.NONCE
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OutDoorLocationsApiService {
    companion object {
        const val APP_USER = BuildConfig.APP_USER
        const val APP_USER_PWD = BuildConfig.APP_USER_PWD
        const val OUTDOOR_LOCATIONS_INFO_METHOD = "OutLocInfo"
    }

    @GET("index.php/api/router")
    fun fetchOtherOutDoorLocations(
        @Query("app_id") app_id: Int = 1,
        @Query("method") method: String = OUTDOOR_LOCATIONS_INFO_METHOD,
        @Query("nonce") nonce: String = NONCE,
        @Query("user") user: String = APP_USER,
        @Query("pwd") pwd: String = APP_USER_PWD
    ): Call<OutDoorLocationsOtherResponse>

    @GET("us_resp.php")
    fun fetchAmericanOutDoorLocations(
    ): Call<List<OutDoorLocationAmerica>>

    @GET("tw_resp.php")
    fun fetchTaiwanOutDoorLocations(
    ): Call<List<OutDoorLocationTaiwan>>
}
