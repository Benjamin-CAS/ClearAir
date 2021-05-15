package com.android_dev.cleanairspaces.persistence.api.services

import com.android_dev.cleanairspaces.utils.API_APP_ID
import com.android_dev.cleanairspaces.utils.NONCE
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface LoggerService {

    @POST("index.php/api/approuter")
    fun sendLogs(
            @Query("app_id") app_id: Int = API_APP_ID,
            @Query("method") method: String = "AndroidApp",
            @Query("nonce") nonce: String = NONCE,
            @Body data: JsonObject
    ): Call<Any>

    @POST("restapi/loc.php")
    fun sendUserLocation(
            @Query("uid") uid: String,
            @Query("lat") lat: String,
            @Query("lon") lon: String,
            @Query("app") app: String = "a",
    ): Call<Any>
}