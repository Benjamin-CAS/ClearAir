package com.android_dev.cleanairspaces.persistence.api.responses

import android.os.Parcelable
import com.android_dev.cleanairspaces.utils.BASE_URL
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationDataFromQr(
        var company_id: String = "",
        var location_id: String = "",
        var company: String = "",
        var location: String = "",
        var dev_name: String = "",
        var is_secure: Boolean = false,
        var logo: String = "",
        var reference_mon: String = "",
        var monitor_id: String = "",
        var type: String? = null,
) : Parcelable {
    fun getFullLogoUrl(): String {
        return LOGO_BASE_URL + logo
    }

    fun getFullDeviceLogoUrl(deviceLogoName: String): String {
        return LOGO_BASE_URL + deviceLogoName
    }

    companion object {
        private const val LOGO_BASE_URL = "${BASE_URL}assets/images/logo/"
        const val RESPONSE_MONITOR_TYPE_KEY = "type"
    }
}