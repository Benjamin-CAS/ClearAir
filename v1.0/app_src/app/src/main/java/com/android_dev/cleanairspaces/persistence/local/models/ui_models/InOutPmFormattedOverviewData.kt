package com.android_dev.cleanairspaces.persistence.local.models.ui_models

import android.content.Context
import android.os.Parcelable
import androidx.core.content.ContextCompat
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class InOutPmFormattedOverviewData(
    val locationName: String, //
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
    val indoorPmValueConverted: Int?,
    val co2Color: Int = -1,
    val vocColor: Int = -1,
    val tmpColor: Int = -1,
    val humldColor: Int = -1
) : Parcelable

fun formatWatchedHighLightsData(
    ctx: Context,
    location: WatchedLocationHighLights,
    aqiIndex: String?
): InOutPmFormattedOverviewData {
    val name = location.name
    val logo = location.getFullLogoUrl()
    val updated =
        ctx.getString(R.string.updated_on_prefix) + " " + location.getUpdatedOnFormatted()
    val locationArea =
        if (location.location_area.isNotBlank()) {
            val prefix = if (location.isIndoorLoc) {
                ctx.getString(R.string.outdoor_txt) //todo indoor?
            } else {
                ctx.getString(R.string.outdoor_txt)
            }
            prefix + "\n" + location.location_area
        } else null
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
    val indoorPmValueConverted = indoorPmValue?.toInt()

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

fun formatMonitorData(
    ctx: Context,
    monitor: MonitorDetails,
    aqiIndex: String?
): InOutPmFormattedOverviewData {
    val name = monitor.indoor_name_en
    val updated = monitor.getUpdatedOnFormatted()
    val pm25Txt = ctx.getString(R.string.default_aqi_pm_2_5)
    val aqiIndexStr = aqiIndex ?: pm25Txt
    val outDoorPmValue = monitor.outdoor_pm
    val hasOutDoorData = (outDoorPmValue != null)
    val outDoorAqiStatus = if (hasOutDoorData) getAQIStatusFromPM25(outDoorPmValue!!, aqiIndexStr)
    else null
    val defaultBgColor = ContextCompat.getColor(ctx, R.color.cas_blue)

    //if we have indoor pm
    val indoorPmValue = monitor.indoor_pm_25
    val hasInDoorData = (indoorPmValue != null)
    val indoorAQIStatus = if (hasInDoorData)
        getAQIStatusFromPM25(indoorPmValue!!, aqiIndexStr)
    else null

    //other data
    val co2Lvl = monitor.indoor_co2
    val vocLvl = monitor.indoor_tvoc
    val tmpLvl = monitor.indoor_temperature
    val humidLvl = monitor.indoor_humidity
    val co2Color = if (co2Lvl != null)
        getColorResFromCO2(co2Lvl) else R.color.aqi_beyond
    val vocColor = if (vocLvl != null)
        getColorResFromVoc(vocLvl) else R.color.aqi_beyond
    val tmpColor = if (tmpLvl != null)
        getColorResFromTmp(tmpLvl) else R.color.aqi_beyond
    val humidColor = if (humidLvl != null)
        getColorResFromHumid(humidLvl) else R.color.aqi_beyond

    return InOutPmFormattedOverviewData(
        locationName = name,
        logo = "",
        updated = updated,
        locationArea = "",
        pm25Txt = pm25Txt,
        aqiIndexStr = aqiIndexStr,
        outDoorPmValue = outDoorPmValue?.toInt(),
        hasOutDoorData = hasOutDoorData,
        outDoorAqiStatus = outDoorAqiStatus,
        defaultBgColor = defaultBgColor,
        indoorPmValue = indoorPmValue?.toInt(),
        hasInDoorData = hasInDoorData,
        indoorAQIStatus = indoorAQIStatus,
        indoorPmValueConverted = 0,
        co2Color = co2Color,
        vocColor = vocColor,
        tmpColor = tmpColor,
        humldColor = humidColor
    )
}


