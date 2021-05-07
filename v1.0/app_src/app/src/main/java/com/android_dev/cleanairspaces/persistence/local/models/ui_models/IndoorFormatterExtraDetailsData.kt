package com.android_dev.cleanairspaces.persistence.local.models.ui_models

import android.content.Context
import android.os.Parcelable
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.IndoorFormatterExtraDetailsData.Companion.sliderMaxForSixGradientLvls
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.IndoorFormatterExtraDetailsData.Companion.sliderMin
import com.android_dev.cleanairspaces.utils.*
import kotlinx.parcelize.Parcelize
import kotlin.math.truncate

@Parcelize
data class IndoorFormatterExtraDetailsData(
        val co2LvlTxt: String,
        val vocLvlTxt: String,
        val tmpLvlTxt: String,
        val humidLvlTxt: String,
        val pmSliderValue: Int,
        val co2SliderValue: Int?,
        val coSliderDiskRes: Int?,
        val tmpSliderValue: Int?,
        val tmpSliderDiskRes: Int?,
        val vocSliderValue: Int?,
        val vocSliderDiskRes: Int?,
        val humidSliderValue: Int?,
        val humidSliderDiskRes: Int?,
        var carbonSavedStr: String,
        var energySavedStr: String
) : Parcelable {
    companion object {
        const val sliderMin = 0
        const val sliderMaxFor3GradientLvls = 100
        const val sliderMaxForSixGradientLvls = 170
    }
}

fun formatWatchedHighLightsIndoorExtras(
        ctx: Context,
        co2Lvl: Double?,
        vocLvl: Double?,
        tmpLvl: Double?,
        humidLvl: Double?,
        inDoorAqiStatus: AQIStatus,
        energyMonth: Double?,
        energyMax: Double?
): IndoorFormatterExtraDetailsData {
    //initialize --
    val co2LvlTxt: String
    val vocLvlTxt: String
    val tmpLvlTxt: String
    val humidLvlTxt: String
    var co2SliderValue: Int? = null
    var coSliderDiskRes: Int? = null
    var tmpSliderValue: Int? = null
    var tmpSliderDiskRes: Int? = null
    var vocSliderValue: Int? = null
    var vocSliderDiskRes: Int? = null
    var humidSliderValue: Int? = null
    var humidSliderDiskRes: Int? = null

    val pmSliderValue: Int = when (inDoorAqiStatus.diskRes) {
        R.drawable.green_seekbar_thumb -> 15
        R.drawable.yellow_seekbar_thumb -> 55
        R.drawable.red_seekbar_thumb -> 85
        else -> R.drawable.beyond_seekbar_thumb
    }

    co2LvlTxt = if (co2Lvl != null) {
        co2SliderValue = getCO2LvlIn100Scale(co2Lvl)
        coSliderDiskRes = getDiskResFromCO2(co2Lvl)
        "$co2Lvl ${ctx.getString(R.string.co2_units)}"

    } else ""

    vocLvlTxt = if (vocLvl != null) {
        vocSliderValue = getTVocLvLIn100Scale(vocLvl)
        vocSliderDiskRes = getDiskResFromTVoc(vocLvl)

        "$vocLvl ${ctx.getString(R.string.tvoc_units)}"

    } else ""

    tmpLvlTxt = if (tmpLvl != null) {
        tmpSliderValue = when {
            tmpLvl.toDouble() <= 13 -> sliderMin
            tmpLvl.toDouble() >= 31 -> sliderMaxForSixGradientLvls
            else -> ((tmpLvl.toDouble() - 13) * 10).toInt()
        }
        tmpSliderDiskRes = getDiskResFromTmp(tmpLvl)

        "$tmpLvl ${ctx.getString(R.string.tmp_units)}"

    } else ""

    humidLvlTxt = if (humidLvl != null) {
        humidSliderValue = when {
            humidLvl.toDouble() <= 25 -> sliderMin
            humidLvl.toDouble() >= 85 -> sliderMaxForSixGradientLvls
            else -> ((humidLvl.toDouble() - 25) * 3).toInt()
        }
        humidSliderDiskRes = getDiskResFromHumid(humidLvl.toDouble())

        "$humidLvl ${ctx.getString(R.string.humid_units)}"

    } else ""

    /******* energy savings *******/
    val carbonSaved: Double
    var carbonSavedStr: String = ""
    var energySavedStr: String = ""
    if (energyMax != null && energyMonth != null) {
        if (energyMax > 0) {
            val maxEnInKW = energyMax / 1000

            carbonSaved = truncate(maxEnInKW * (energyMonth / 100) * 0.28)
            carbonSavedStr = if (carbonSaved > 1000) {
                val carbonSavedKg = carbonSaved / 1000
                "$carbonSavedKg ${ctx.getString(R.string.tonnes_txt)}"
            } else {
                "$carbonSaved ${ctx.getString(R.string.kg_txt)}"
            }
            energySavedStr = "$energyMonth ${ctx.getString(R.string.percent)}"

        }
    }

    return IndoorFormatterExtraDetailsData(
            co2LvlTxt = co2LvlTxt,
            vocLvlTxt = vocLvlTxt,
            tmpLvlTxt = tmpLvlTxt,
            humidLvlTxt = humidLvlTxt,
            pmSliderValue = pmSliderValue,
            co2SliderValue = co2SliderValue,
            coSliderDiskRes = coSliderDiskRes,
            tmpSliderValue = tmpSliderValue,
            tmpSliderDiskRes = tmpSliderDiskRes,
            vocSliderValue = vocSliderValue,
            vocSliderDiskRes = vocSliderDiskRes,
            humidSliderValue = humidSliderValue,
            humidSliderDiskRes = humidSliderDiskRes,
            carbonSavedStr = carbonSavedStr,
            energySavedStr = energySavedStr
    )
}

