package com.android_dev.cleanairspaces.persistence.api.responses

import com.google.gson.annotations.Expose

/************* INDOOR **********/
data class IndoorLocationsDetailsResponse(
        @Expose
        val payload: String?,

        @Expose
        val ltime: String?

)

data class IndoorLocationExtraDetails(
        val location_id: String,
        val name_en: String,
        val active: Number,
        val outdoor: String?,
        val logo: String,
)

data class IndoorLocationsResponse(
        @Expose
        val status: Boolean,
        @Expose
        val code: Int,
        @Expose
        val data: List<IndoorLocations>
)

data class IndoorLocations(
        @Expose
        val company_id: String,
        @Expose
        val name_en: String,
        @Expose
        val secure: Number,
        @Expose
        val active: Number,
        @Expose
        val outdoor: Number,
)
