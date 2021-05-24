package com.android_dev.cleanairspaces.persistence.api.responses

import com.google.gson.annotations.Expose

data class DevicesDetailsResponse(
    @Expose
    val payload: String?,

    @Expose
    val ltime: String?
)