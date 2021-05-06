package com.android_dev.cleanairspaces.persistence.api.responses

import com.google.gson.annotations.Expose

data class LocationDetailsResponse(
    @Expose
    val payload: String?,

    @Expose
    val code: String,

    @Expose
    val ltime: String?
)

