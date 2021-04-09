package com.cleanairspaces.android.utils

import android.content.Context
import android.os.Parcelable
import com.cleanairspaces.android.R
import com.cleanairspaces.android.models.entities.CustomerDeviceDataDetailed
import kotlinx.parcelize.Parcelize


@Parcelize
data class LocationDetailsInfo(
    val locationArea: String,
    val dataDetailed: CustomerDeviceDataDetailed,
    val aqiIndex: String,
    val bgColor: Int,
    val inStatusIndicatorRes: Int,
    val inStatusTvTxt: String,
    val inPmValue: String,
    val outStatusIndicatorRes: Int,
    val outStatusTvTxt: String,
    val outPmValue: String,
    val updatedOnTxt: String
) : Parcelable

/********* parses location info into text suitable for display in the ui ******/
fun getLocationInfoDetails(ctx : Context,
                           dataDetailed: CustomerDeviceDataDetailed,
                           selectedAqiIndex : String?
): LocationDetailsInfo {

    val myLocationDetails = dataDetailed.locationDetails
    val location = dataDetailed.deviceData

    val aqiIndex: String =
        selectedAqiIndex ?: ctx.getString(R.string.default_pm_index_value)

    val locationArea =
        ctx.getString(R.string.outdoor_txt) + ": " + location.location


    val outDoorPm = myLocationDetails.outdoor.outdoor_pm.toDouble()
    val (outStatusIndicatorRes, outStatusText, outPmValue) =
        if (aqiIndex == "PM2.5" || aqiIndex == "AQI US") {
            Triple(
                MyColorUtils.convertUIColorToStatusRes(
                    AQI.getAQIStatusColorFromPM25(
                        outDoorPm
                    )
                ),
                AQI.getAQIStatusTextFromPM25(outDoorPm),
                outDoorPm.toString()
            )
        } else {
            Triple(
                MyColorUtils.convertUIColorToStatusRes(
                    AQI.getAQICNStatusColorFromPM25(
                        outDoorPm
                    )
                ),
                AQI.getAQICNStatusTextFromPM25(outDoorPm),
                AQI.getAQICNFromPM25(outDoorPm).toString()
            )
        }


    val outStatusTvTxt = ctx.getString(outStatusText.conditionStrRes)


    val inDoorPm = myLocationDetails.indoor.indoor_pm.toDouble()
    val (inStatusIndicatorRes, inStatusText, inPmValue) =
        if (aqiIndex == "PM2.5" || aqiIndex == "AQI US") {
            Triple(
                MyColorUtils.convertUIColorToStatusRes(
                    AQI.getAQIStatusColorFromPM25(
                        inDoorPm
                    )
                ),
                AQI.getAQIStatusTextFromPM25(inDoorPm),
                inDoorPm.toString()
            )
        } else {
            Triple(
                MyColorUtils.convertUIColorToStatusRes(
                    AQI.getAQICNStatusColorFromPM25(
                        inDoorPm
                    )
                ),
                AQI.getAQICNStatusTextFromPM25(inDoorPm),
                AQI.getAQICNFromPM25(inDoorPm).toString()
            )
        }

    val inStatusTvTxt = ctx.getString(inStatusText.conditionStrRes)




    val bgColor = if (aqiIndex == "PM2.5" || aqiIndex == "AQI US") {
        when {
            AQI.getAQIFromPM25(inDoorPm) < 100 -> R.color.dark_green

            AQI.getAQIFromPM25(inDoorPm) > 150 -> R.color.red
            else -> R.color.orange
        }

    } else {
        when {
            AQI.getAQICNFromPM25(inDoorPm) < 150 -> R.color.dark_green
            AQI.getAQICNFromPM25(inDoorPm) > 200 -> R.color.red
            else -> R.color.orange
        }
    }


    val updatedOnTxt =
        ctx.getString(R.string.updated_on_prefix) + "\n" + myLocationDetails.getFormattedUpdateTime()

    return LocationDetailsInfo(
        locationArea,
        dataDetailed,
        aqiIndex,
        bgColor,
        inStatusIndicatorRes,
        inStatusTvTxt,
        inPmValue,
        outStatusIndicatorRes,
        outStatusTvTxt,
        outPmValue,
        updatedOnTxt
    )

}