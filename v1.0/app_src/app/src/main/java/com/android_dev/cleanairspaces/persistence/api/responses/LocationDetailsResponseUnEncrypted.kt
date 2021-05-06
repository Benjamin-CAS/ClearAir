package com.android_dev.cleanairspaces.persistence.api.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class LocationDetails(
    var company_id: String = "",
    var location_id: String = "",
    var lastUpdated: Long = System.currentTimeMillis(),
    val indoor: Indoor?,
    val outdoor: Outdoor?,
    val energy: Energy?,
    val ExpFilter: String? = "",
    val ExpEquip: String? = "",
    var lastKnownUserName: String = "", //for refreshing
    var lastKnownPassword: String = "", //for refreshing
) : Parcelable {
    fun getLat(): Double? {
        return (indoor?.lat ?: outdoor?.lat)?.toDouble()
    }

    fun getLon(): Double? {
        return (indoor?.lon ?: outdoor?.lon)?.toDouble()
    }
}

@Parcelize
data class Indoor(
    val indoor_pm: String = "",
    val indoor_co2: String = "",
    val indoor_voc: String = "",
    val indoor_temperature: String = "",
    val indoor_humidity: String = "",
    val indoor_time: String? = "",
    val param_label: String? = "",
    val display_param: String? = "",
    val name_en: String? = "",
    val lon: String? = "",
    val lat: String? = "",
) : Parcelable

@Parcelize
data class Outdoor(
    val outdoor_pm: String = "",
    val outdoor_time: String? = "",
    val outdoor_display_param: String? = "",
    val name_en: String? = "",
    val lon: String? = "",
    val lat: String? = "",
) : Parcelable

@Parcelize
data class Energy(
    val month: String? = "",
    val max: String? = "",
    val current_used: String? = "",
    val current_max: String? = "",
) : Parcelable
