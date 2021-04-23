package com.android_dev.cleanairspaces.persistence.api.responses

import com.google.gson.annotations.Expose

/************* INDOOR **********/
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
