package com.cleanairspaces.android.models.api.responses

import com.google.gson.annotations.Expose

data class ScannedDeviceQrResponse(
    @Expose
    val payload: String?,

    @Expose
    val code: String,

    @Expose
    val ltime: String?

)