package com.android_dev.cleanairspaces.persistence.api.services

import com.android_dev.cleanairspaces.persistence.api.responses.LocationDetailsResponse
import com.android_dev.cleanairspaces.utils.API_APP_ID
import com.android_dev.cleanairspaces.utils.NONCE
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface LocationDetailsService {


    @POST("index.php/api/approuter")
    fun fetchDetailsForLocation(
            @Query("app_id") app_id: Int = API_APP_ID,
            @Query("method") method: String,
            @Query("nonce") nonce: String = NONCE,
            @Body data: JsonObject
    ): Call<LocationDetailsResponse>

}