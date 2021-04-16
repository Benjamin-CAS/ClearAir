package com.cleanairspaces.android.utils

import android.content.Context
import android.os.Parcelable
import com.cleanairspaces.android.R
import com.cleanairspaces.android.models.entities.LocationDetailsGeneralDataWrapper
import com.cleanairspaces.android.utils.MyColorUtils.convertUIColorToColorRes
import kotlinx.parcelize.Parcelize
import kotlin.math.truncate


@Parcelize
data class MyLocationDetailsWrapper(
    val locationArea: String,
    val wrappedData: LocationDetailsGeneralDataWrapper,
    val aqiIndex: String,
    val bgColor: Int,
    val inStatusIndicatorRes: Int,
    val inStatusTvTxt: String,
    val inPmValueTxt: String,
    val outStatusIndicatorRes: Int,
    val outStatusTvTxt: String,
    val outPmValueTxt: String,
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
    val pmSliderMax: Int,
    val pmSliderValue: Int,
    val co2SliderMin: Int,
    val co2SliderMax: Int,
    val co2Slider: Int = UNSET_PARAM_VAL,
    val coSliderDiskRes: Int,
    val tmpSliderMin: Int,
    val tmpSliderMax: Int,
    val tmpSlider: Int = UNSET_PARAM_VAL,
    val tmpSliderDiskRes: Int,
    val vocSliderMin: Int,
    val vocSliderMax: Int,
    val vocSlider: Int = UNSET_PARAM_VAL,
    val vocSliderDiskRes: Int,
    val humidSliderMin: Int,
    val humidSliderMax: Int,
    val humidSlider: Int = UNSET_PARAM_VAL,
    val humidSliderDiskRes: Int,
    val carbonSavedStr: String = "",
    val energySavedStr: String = "",
    val outDoorPmTxtColor: Int = UNSET_PARAM_VAL,
    val indoorPmValue: Int = UNSET_PARAM_VAL

) : Parcelable

/********* parses location info into text suitable for display in the ui ******/
fun getLocationInfoDetails(
    ctx: Context,
    dataWrapper: LocationDetailsGeneralDataWrapper,
    selectedAqiIndex: String?
): MyLocationDetailsWrapper {

    val myLocationDetails = dataWrapper.locationDetails

    val pm25Default = ctx.getString(R.string.default_pm_index_value)
    val aqiUs = ctx.getString(R.string.us_aqi_index_value)
    val aqiIndex: String =
        selectedAqiIndex ?: pm25Default

    val locationArea =
        ctx.getString(R.string.outdoor_txt) + ": " + myLocationDetails.outdoor.name_en

    var inDoorPmValue: Double = UNSET_PARAM_VAL.toDouble()
    var inDoorPmTxt = ""
    //defaults -- wont actually be displayed
    var bgColor = 0
    var inStatusIndicatorRes = 0
    var inStatusTvTxt = ""
    var inPmValueTxt = ""
    var inBgTransparent = 0
    val inStatusText: ResourceCommentWrapper?
    val hasIndoorData = myLocationDetails.indoor.indoor_pm.isNotBlank()
    if (hasIndoorData) {
        inDoorPmValue = myLocationDetails.indoor.indoor_pm.toDouble()
        inDoorPmTxt = "$inDoorPmValue ${ctx.getString(R.string.pm_units)}"
        if (aqiIndex == pm25Default || aqiIndex == aqiUs) {
            inStatusIndicatorRes = MyColorUtils.convertUIColorToStatusRes(
                AQI.getAQIStatusColorFromPM25(
                    inDoorPmValue
                )
            )
            inStatusText = AQI.getAQIStatusTextFromPM25(inDoorPmValue)
            inPmValueTxt = inDoorPmValue.toString()
        } else {

            inStatusIndicatorRes = MyColorUtils.convertUIColorToStatusRes(
                AQI.getAQICNStatusColorFromPM25(
                    inDoorPmValue
                )
            )
            inStatusText = AQI.getAQICNStatusTextFromPM25(inDoorPmValue)
            inPmValueTxt = AQI.getAQICNFromPM25(inDoorPmValue).toString()

        }
        inStatusTvTxt = ctx.getString(inStatusText.resourceId)
        if (aqiIndex == pm25Default || aqiIndex == aqiUs) {
            when {
                AQI.getAQIFromPM25(inDoorPmValue) < 100 -> {
                    bgColor = R.color.dark_green
                    inBgTransparent = R.color.transparent_green
                }

                AQI.getAQIFromPM25(inDoorPmValue) > 150 -> {
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
                AQI.getAQICNFromPM25(inDoorPmValue) < 150 -> {
                    bgColor = R.color.dark_green
                    inBgTransparent = R.color.transparent_green
                }

                AQI.getAQICNFromPM25(inDoorPmValue) > 200 -> {
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
    var outDoorPmTxtColor = UNSET_PARAM_VAL
    val (outStatusIndicatorRes, outStatusText, outPmValueTxt) =
        if (aqiIndex == pm25Default || aqiIndex == aqiUs) {
            val statusColor = AQI.getAQIStatusColorFromPM25(
                outDoorPm
            )
            outDoorPmTxtColor = convertUIColorToColorRes(statusColor)
            Triple(
                MyColorUtils.convertUIColorToStatusRes(
                    statusColor
                ),
                AQI.getAQIStatusTextFromPM25(outDoorPm),
                outDoorPm.toString()
            )
        } else {
            val statusColor = AQI.getAQICNStatusColorFromPM25(
                outDoorPm
            )
            outDoorPmTxtColor = convertUIColorToColorRes(statusColor)
            Triple(
                MyColorUtils.convertUIColorToStatusRes(
                    statusColor
                ),
                AQI.getAQICNStatusTextFromPM25(outDoorPm),
                AQI.getAQICNFromPM25(outDoorPm).toString()
            )
        }


    val outStatusTvTxt = ctx.getString(outStatusText.resourceId)

    val (outBgTransparent, pmSliderDiskRes) = if (aqiIndex == pm25Default || aqiIndex == aqiUs) {
        when {
            AQI.getAQIFromPM25(outDoorPm) < 100 ->
                Pair(R.color.transparent_green, R.drawable.green_seekbar_thumb)

            AQI.getAQIFromPM25(outDoorPm) > 150 -> Pair(
                R.color.transparent_red,
                R.drawable.red_seekbar_thumb
            )
            else -> Pair(R.color.transparent_orange, R.drawable.yellow_seekbar_thumb)
        }

    } else {
        when {
            AQI.getAQICNFromPM25(outDoorPm) < 150 -> Pair(
                R.color.transparent_green,
                R.drawable.green_seekbar_thumb
            )
            AQI.getAQICNFromPM25(outDoorPm) > 200 -> Pair(
                R.color.transparent_red,
                R.drawable.red_seekbar_thumb
            )
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

    val co2SliderMin = 10
    val co2SliderMax = 170
    var co2Slider = UNSET_PARAM_VAL
    var coSliderDiskRes = UNSET_PARAM_VAL

    val tmpSliderMin = 10
    val tmpSliderMax = 170
    var tmpSlider = UNSET_PARAM_VAL
    var tmpSliderDiskRes = UNSET_PARAM_VAL

    val vocSliderMin = 10
    val vocSliderMax = 170
    var vocSlider = UNSET_PARAM_VAL
    var vocSliderDiskRes = UNSET_PARAM_VAL

    val humidSliderMin = 10
    val humidSliderMax = 170
    var humidSlider = UNSET_PARAM_VAL
    var humidSliderDiskRes = UNSET_PARAM_VAL

    var carbonSavedStr: String = ""
    var energySavedStr: String = ""

    /******** INDOOR DATA *******/
    if (hasIndoorData) {

        pmSliderValue = when {
            inDoorPmValue <= 5 -> {
                pmSliderMin
            }
            inDoorPmValue >= 55 -> {
                pmSliderMax
            }
            else -> {
                ((inDoorPmValue - 5) * 3.6).toInt()
            }
        }

        val co2Lvl = myLocationDetails.indoor.indoor_co2
        val vocLvl = myLocationDetails.indoor.indoor_voc
        val tmpLvl = myLocationDetails.indoor.indoor_temperature
        val humidLvl = myLocationDetails.indoor.indoor_humidity

        co2LvlTxt = if (co2Lvl.isNotBlank()) {
            co2Slider = when {
                co2Lvl.toDouble() <= 500 -> co2SliderMin
                co2Lvl.toDouble() >= 1250 -> co2SliderMax
                else -> ((co2Lvl.toDouble() - 500) * 0.24).toInt()
            }
            coSliderDiskRes = AQI.getDiskResFromCO2(co2Lvl.toDouble())

            "$co2Lvl ${ctx.getString(R.string.co2_units)}"

        } else ""

        vocLvlTxt = if (vocLvl.isNotBlank()) {
            vocSlider = when {
                vocLvl.toDouble() <= 0.55 -> vocSliderMin
                vocLvl.toDouble() >= 0.85 -> vocSliderMax
                else -> ((vocLvl.toDouble() - 0.55) * 600).toInt()
            }
            vocSliderDiskRes = AQI.getDiskResFromVoc(vocLvl.toDouble())

            "$vocLvl ${ctx.getString(R.string.tvoc_units)}"

        } else ""

        tmpLvlTxt = if (tmpLvl.isNotBlank()) {
            tmpSlider = when {
                tmpLvl.toDouble() <= 13 -> tmpSliderMin
                tmpLvl.toDouble() >= 31 -> tmpSliderMax
                else -> ((tmpLvl.toDouble() - 13) * 10).toInt()
            }
            tmpSliderDiskRes = AQI.getDiskResFromTmp(tmpLvl.toDouble())

            "$tmpLvl ${ctx.getString(R.string.tmp_units)}"

        } else ""

        humidLvlTxt = if (humidLvl.isNotBlank()) {
            humidSlider = when {
                humidLvl.toDouble() <= 25 -> humidSliderMin
                humidLvl.toDouble() >= 85 -> humidSliderMax
                else -> ((humidLvl.toDouble() - 25) * 3).toInt()
            }
            humidSliderDiskRes = AQI.getDiskResFromHumid(humidLvl.toDouble())

            "$humidLvl ${ctx.getString(R.string.humid_units)}"

        } else ""

        /******* energy savings *******/
        val energy = myLocationDetails.energy
        val carbonSaved: Double
        if (!energy.max.isNullOrBlank() && !energy.month.isNullOrBlank()) {
            val energyMax = energy.max.toDouble()
            if (energyMax > 0) {
                val maxEnInKW = energyMax / 1000

                carbonSaved = truncate(maxEnInKW * (energy.month.toDouble() / 100) * 0.28)
                carbonSavedStr = if (carbonSaved > 1000) {
                    val carbonSavedKg = carbonSaved / 1000
                    "$carbonSavedKg ${ctx.getString(R.string.tonnes_txt)}"
                } else {
                    "$carbonSaved ${ctx.getString(R.string.kg_txt)}"
                }
                energySavedStr = "${energy.month} ${ctx.getString(R.string.percent)}"

            }
        }

    }

    return MyLocationDetailsWrapper(
        locationArea = locationArea,
        wrappedData = dataWrapper,
        aqiIndex = aqiIndex,
        bgColor = bgColor,
        inStatusIndicatorRes = inStatusIndicatorRes,
        inStatusTvTxt = inStatusTvTxt,
        inPmValueTxt = inPmValueTxt,
        outStatusIndicatorRes = outStatusIndicatorRes,
        outStatusTvTxt = outStatusTvTxt,
        outPmValueTxt = outPmValueTxt,
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
        pmSliderValue = pmSliderValue,
        co2SliderMin = co2SliderMin,
        co2SliderMax = co2SliderMax,
        co2Slider = co2Slider,
        coSliderDiskRes = coSliderDiskRes,
        tmpSliderMin = tmpSliderMin,
        tmpSliderMax = tmpSliderMax,
        tmpSlider = tmpSlider,
        tmpSliderDiskRes = tmpSliderDiskRes,
        vocSliderMin = vocSliderMin,
        vocSliderMax = vocSliderMax,
        vocSlider = vocSlider,
        vocSliderDiskRes = vocSliderDiskRes,
        humidSliderMin = humidSliderMin,
        humidSliderMax = humidSliderMax,
        humidSlider = humidSlider,
        humidSliderDiskRes = humidSliderDiskRes,
        carbonSavedStr = carbonSavedStr,
        energySavedStr = energySavedStr,
        outDoorPmTxtColor = outDoorPmTxtColor,
        indoorPmValue = inDoorPmValue.toInt()
    )

}

fun getStatusColorForPm(ctx: Context, aqiIndex : String, pmValue : Double): Int{
    val pm25Default = ctx.getString(R.string.default_pm_index_value)
    val aqiUs = ctx.getString(R.string.us_aqi_index_value)

    if (aqiIndex == pm25Default || aqiIndex == aqiUs) {
       return  when {
            AQI.getAQIFromPM25(pmValue) < 100 -> {
                R.color.aqi_good
            }

            AQI.getAQIFromPM25(pmValue) > 150 -> {
               R.color.aqi_hazardous
            }
            else -> {
                R.color.aqi_moderate
            }
        }

    } else {
        return when {
            AQI.getAQICNFromPM25(pmValue) < 150 -> {
                R.color.aqi_good
            }

            AQI.getAQICNFromPM25(pmValue) > 200 -> {
                R.color.aqi_hazardous
            }
            else -> {
                R.color.aqi_moderate
            }
        }
    }
}


const val UNSET_PARAM_VAL = -1
private const val TAG = "LocationDetailsDisplayInfoGenerator"