package com.android_dev.cleanairspaces.persistence.api.responses


import com.google.gson.annotations.Expose

data class ScannedDeviceQrWithCompLocResponse(
    @Expose
    val payload: String?,

    @Expose
    val code: String,

    @Expose
    val ltime: String?
) {
    companion object {
        const val response_key = "customer"
    }
}
