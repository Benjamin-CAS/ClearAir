package com.android_dev.cleanairspaces.persistence.local.models.ui_models

import android.content.Context
import android.os.Parcelable
import androidx.core.content.ContextCompat
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.AQIStatus
import com.android_dev.cleanairspaces.utils.getAQIStatusFromPM25
import kotlinx.parcelize.Parcelize

@Parcelize
data class InOutPmFormattedOverviewData(
    val locationName: String,
    val logo: String,
    val updated: String,
    val locationArea: String?,
    val pm25Txt: String,
    val aqiIndexStr: String,
    val outDoorPmValue: Int?,
    val hasOutDoorData: Boolean,
    val outDoorAqiStatus: AQIStatus?,
    val defaultBgColor: Int,
    val indoorPmValue: Int?,
    val hasInDoorData: Boolean,
    val indoorAQIStatus: AQIStatus?,
    val indoorPmValueConverted: Double?
) : Parcelable

fun formatWatchedHighLightsData(
    ctx: Context,
    location: WatchedLocationHighLights,
    aqiIndex: String?
): InOutPmFormattedOverviewData {
    val name = location.name
    val logo = location.getFullLogoUrl()
    val updated =
        ctx.getString(R.string.updated_on_prefix) + "\n" + location.getUpdatedOnFormatted()
    val locationArea =
        if (location.location_area.isNotBlank()) ctx.getString(R.string.outdoor_txt) + "\n" + location.location_area
        else null
    val pm25Txt = ctx.getString(R.string.default_aqi_pm_2_5)
    val aqiIndexStr = aqiIndex ?: pm25Txt
    val outDoorPmValue = location.pm_outdoor
    val hasOutDoorData = (outDoorPmValue != null)
    val outDoorAqiStatus = if (hasOutDoorData) getAQIStatusFromPM25(outDoorPmValue!!, aqiIndexStr)
    else null
    val defaultBgColor = ContextCompat.getColor(ctx, R.color.blackish)

    //if we have indoor pm
    val indoorPmValue = location.pm_indoor
    val hasInDoorData = (indoorPmValue != null)
    val indoorAQIStatus = if (hasInDoorData)
        getAQIStatusFromPM25(indoorPmValue!!, aqiIndexStr)
    else null

    //todo --? I think this is AQI not PM--
    val indoorPmValueConverted = indoorPmValue

    return InOutPmFormattedOverviewData(
        locationName = name,
        logo = logo,
        updated = updated,
        locationArea = locationArea,
        pm25Txt = pm25Txt,
        aqiIndexStr = aqiIndexStr,
        outDoorPmValue = outDoorPmValue?.toInt(),
        hasOutDoorData = hasOutDoorData,
        outDoorAqiStatus = outDoorAqiStatus,
        defaultBgColor = defaultBgColor,
        indoorPmValue = indoorPmValue?.toInt(),
        hasInDoorData = hasInDoorData,
        indoorAQIStatus = indoorAQIStatus,
        indoorPmValueConverted = indoorPmValueConverted
    )
}

