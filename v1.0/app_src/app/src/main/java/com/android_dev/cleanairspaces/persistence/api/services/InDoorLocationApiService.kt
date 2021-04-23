package com.android_dev.cleanairspaces.persistence.api.services

import com.android_dev.cleanairspaces.persistence.api.responses.IndoorLocationsResponse
import com.android_dev.cleanairspaces.utils.API_APP_ID
import com.android_dev.cleanairspaces.utils.NONCE
import retrofit2.Call
import retrofit2.http.Body
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

}