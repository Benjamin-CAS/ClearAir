package com.cleanairspaces.android.utils

import android.content.Context
import android.os.Parcelable
import com.cleanairspaces.android.R
import com.cleanairspaces.android.models.entities.LocationDetailsGeneralDataWrapper
import kotlinx.parcelize.Parcelize


@Parcelize
data class MyLocationDetailsWrapper(
        val locationArea: String,
        val wrappedData: LocationDetailsGeneralDataWrapper,
        val aqiIndex: String,
        val bgColor: Int,
        val inStatusIndicatorRes: Int,
        val inStatusTvTxt: String,
        val inPmValue: String,
        val outStatusIndicatorRes: Int,
        val outStatusTvTxt: String,
        val outPmValue: String,
        val updatedOnTxt: String,
        var inBgTransparent: Int = 0,
        var outBgTransparent: Int = 0,
        val co2LvlTxt: String = "",
        val vocLvlTxt: String = "",
        val tmpLvl: String = "",
        val humidLvl: String = "",
        val ogInPmTxt: String,
        val pmSliderDiskRes: Int,
        val pmSliderMin: Int,
        val pmSliderMax:Int,
        val pmSliderValue: Int

) : Parcelable {
}

/********* parses location info into text suitable for display in the ui ******/
fun getLocationInfoDetails(
        ctx: Context,
        dataWrapper: LocationDetailsGeneralDataWrapper,
        selectedAqiIndex: String?
): MyLocationDetailsWrapper {

    val myLocationDetails = dataWrapper.locationDetails
    val location = dataWrapper.generalData

    val pm25Default = ctx.getString(R.string.default_pm_index_value)
    val aqiUs = ctx.getString(R.string.us_aqi_index_value)
    val aqiIndex: String =
            selectedAqiIndex ?: pm25Default

    val locationArea =
            ctx.getString(R.string.outdoor_txt) + ": " + location.location

    var inDoorPm: Double = 0.0
    var inDoorPmTxt : String  = ""
    //defaults -- wont actually be displayed
    var bgColor = 0
    var inStatusIndicatorRes = 0
    var inStatusTvTxt = ""
    var inPmValue = ""
    var inBgTransparent = 0
    val inStatusText: ConditionResStrings?
    val hasIndoorData = myLocationDetails.indoor.indoor_pm.isNotBlank()
    if (hasIndoorData) {
        inDoorPm = myLocationDetails.indoor.indoor_pm.toDouble()
        inDoorPmTxt = "$inDoorPm ${ctx.getString(R.string.pm_units)}"
        if (aqiIndex == pm25Default || aqiIndex == aqiUs) {
            inStatusIndicatorRes = MyColorUtils.convertUIColorToStatusRes(
                    AQI.getAQIStatusColorFromPM25(
                            inDoorPm
                    )
            )
            inStatusText = AQI.getAQIStatusTextFromPM25(inDoorPm)
            inPmValue = inDoorPm.toString()
        } else {

            inStatusIndicatorRes = MyColorUtils.convertUIColorToStatusRes(
                    AQI.getAQICNStatusColorFromPM25(
                            inDoorPm
                    )
            )
            inStatusText = AQI.getAQICNStatusTextFromPM25(inDoorPm)
            inPmValue = AQI.getAQICNFromPM25(inDoorPm).toString()

        }
        inStatusTvTxt = ctx.getString(inStatusText.conditionStrRes)
        if (aqiIndex == pm25Default || aqiIndex == aqiUs) {
            when {
                AQI.getAQIFromPM25(inDoorPm) < 100 -> {
                    bgColor = R.color.dark_green
                    inBgTransparent = R.color.transparent_green
                }

                AQI.getAQIFromPM25(inDoorPm) > 150 -> {
                    bgColor = R.color.red
                    inBgTransparent = R.color.transparent_red
                }
                else -> {
                    bgColor = R.color.orange
                    inBgTransparent = R.color.transparent_orange
                }
            }

        } else {
            when {
                AQI.getAQICNFromPM25(inDoorPm) < 150 -> {
                    bgColor = R.color.dark_green
                    inBgTransparent = R.color.transparent_green
                }

                AQI.getAQICNFromPM25(inDoorPm) > 200 -> {
                    bgColor = R.color.red
                    inBgTransparent = R.color.transparent_red
                }
                else -> {
                    bgColor = R.color.orange
                    inBgTransparent = R.color.transparent_orange
                }
            }
        }
    }
    /*************** OUT DOOR VALUES ************/

    val outDoorPm = myLocationDetails.outdoor.outdoor_pm.toDouble()
    val (outStatusIndicatorRes, outStatusText, outPmValue) =
            if (aqiIndex == pm25Default || aqiIndex == aqiUs) {
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

    val (outBgTransparent, pmSliderDiskRes)  = if (aqiIndex == pm25Default || aqiIndex == aqiUs) {
        when {
            AQI.getAQIFromPM25(outDoorPm) < 100 ->
                Pair(R.color.transparent_green, R.drawable.green_seekbar_thumb)

            AQI.getAQIFromPM25(outDoorPm) > 150 -> Pair(R.color.transparent_red, R.drawable.red_seekbar_thumb)
            else -> Pair(R.color.transparent_orange, R.drawable.yellow_seekbar_thumb)
        }

    } else {
        when {
            AQI.getAQICNFromPM25(outDoorPm) < 150 -> Pair(R.color.transparent_green, R.drawable.green_seekbar_thumb)
            AQI.getAQICNFromPM25(outDoorPm) > 200 -> Pair(R.color.transparent_red, R.drawable.red_seekbar_thumb)
            else -> Pair(R.color.transparent_orange, R.drawable.yellow_seekbar_thumb)
        }
    }


    val updatedOnTxt =
            ctx.getString(R.string.updated_on_prefix) + "\n" + myLocationDetails.getFormattedUpdateTime()


    /********** EXTRAS *********/
    var co2LvlTxt = ""
    var vocLvlTxt = ""
    var tmpLvlTxt = ""
    var humidLvlTxt = ""
    val pmSliderMin = 10
    val pmSliderMax = 170
    var pmSliderValue = 0

    if (hasIndoorData) {
        val co2Lvl = myLocationDetails.indoor.indoor_co2
        val vocLvl = myLocationDetails.indoor.indoor_voc
        val tmpLvl = myLocationDetails.indoor.indoor_temperature
        val humidLvl = myLocationDetails.indoor.indoor_humidity

        co2LvlTxt = if (co2Lvl.isNotBlank()) {
            if (aqiIndex == pm25Default || aqiIndex == aqiUs) {
                "$co2Lvl ${ctx.getString(R.string.co2_units)}"
            } else {
                "${AQI.getAQILevelFromCO2(co2Lvl.toDouble())} ${ctx.getString(R.string.co2_units)}"
            }
        } else ""

        vocLvlTxt = if (vocLvl.isNotBlank()) {
            if (aqiIndex == pm25Default || aqiIndex == aqiUs) {
                "$vocLvl ${ctx.getString(R.string.vco_units)}"
            } else {
                "${AQI.getAQILevelFromVOC(vocLvl.toDouble())} ${ctx.getString(R.string.vco_units)}"
            }
        } else ""

        tmpLvlTxt = if (tmpLvl.isNotBlank()) {
                "$tmpLvl ${ctx.getString(R.string.tmp_units)}"
        } else ""

        humidLvlTxt = if (humidLvl.isNotBlank()) {
                "$humidLvl ${ctx.getString(R.string.humid_units)}"
        } else ""


        /******** sliders ***********/
        pmSliderValue  = when {
            inDoorPm <= 5 -> {
                pmSliderMin
            }
            inDoorPm >= 55 -> {
                pmSliderMax
            }
            else -> {
                ((inDoorPm - 5) * 3.6).toInt()
            }
        }



    }

    return MyLocationDetailsWrapper(
            locationArea = locationArea,
            wrappedData= dataWrapper,
            aqiIndex = aqiIndex,
            bgColor = bgColor,
            inStatusIndicatorRes = inStatusIndicatorRes,
            inStatusTvTxt = inStatusTvTxt,
            inPmValue = inPmValue,
            outStatusIndicatorRes = outStatusIndicatorRes,
            outStatusTvTxt = outStatusTvTxt,
            outPmValue = outPmValue,
            updatedOnTxt = updatedOnTxt,
            inBgTransparent = inBgTransparent,
            outBgTransparent = outBgTransparent,
            co2LvlTxt = co2LvlTxt,
            vocLvlTxt = vocLvlTxt,
            tmpLvl = tmpLvlTxt,
            humidLvl = humidLvlTxt,
            ogInPmTxt = inDoorPmTxt,
            pmSliderDiskRes = pmSliderDiskRes,
            pmSliderMin = pmSliderMin,
                    pmSliderMax = pmSliderMax,
            pmSliderValue = pmSliderValue

    )

}