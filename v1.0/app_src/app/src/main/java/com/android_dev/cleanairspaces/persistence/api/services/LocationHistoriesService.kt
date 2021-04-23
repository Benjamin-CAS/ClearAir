package com.android_dev.cleanairspaces.persistence.api.services

import com.android_dev.cleanairspaces.persistence.api.responses.LocationHistoriesResponse
import com.android_dev.cleanairspaces.utils.API_APP_ID
import com.android_dev.cleanairspaces.utils.NONCE
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface LocationHistoriesService {
    @POST("index.php/api/approuter")
    fun fetchLocationHistory(
            @Query("app_id") app_id: Int = API_APP_ID,
            @Query("method") method: String,
            @Query("nonce") nonce: String = NONCE,
            @Body data: JsonObject
    ): Call<LocationHistoriesResponse>
}