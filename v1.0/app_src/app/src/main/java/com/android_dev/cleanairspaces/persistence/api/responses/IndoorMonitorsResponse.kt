package com.android_dev.cleanairspaces.persistence.api.responses

import android.os.Parcelable
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize

data class IndoorMonitorsResponse(
        @Expose
        val payload: String?,

        @Expose
        val ltime: String?

)

data class MonitorDetailsResponseRoot (
    val monitor: HashMap<String, MonitorDetails>
)

@Parcelize
class MonitorDetails(
        val indoor: IndoorMonitorDetails?,
        val outdoor: OutdoorMonitorDetails?,
) : Parcelable

@Parcelize
data class IndoorMonitorDetails(
        val name_en: String,
        val reading: String,
        val tvoc: String?,
        val co2: String?,
        val temperature: String?,
        val humidity: String?,
        val date: String,
        val display_param: String?
) : Parcelable

@Parcelize
data class OutdoorMonitorDetails(
        val outdoor_pm: String?,
        val outdoor_time: String?,
        val outdoor_display_param: String?,
        val outdoor_name_en: String?,
) : Parcelable