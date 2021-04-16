package com.cleanairspaces.android.utils

import com.cleanairspaces.android.R

object AQI {

    private val TAG = AQI::class.java.simpleName

    val SO2 = doubleArrayOf(0.0, 50.0, 150.0, 475.0, 800.0, 1600.0, 2100.0, 2620.0)
    val NO2 = doubleArrayOf(0.0, 40.0, 80.0, 180.0, 280.0, 565.0, 750.0, 940.0)
    val PM10 = doubleArrayOf(0.0, 50.0, 150.0, 250.0, 350.0, 420.0, 500.0, 600.0)
    val CO = doubleArrayOf(0.0, 2.0, 4.0, 14.0, 24.0, 36.0, 48.0, 60.0)
    val O3 = doubleArrayOf(0.0, 160.0, 200.0, 300.0, 400.0, 800.0, 1000.0, 1200.0)
    val PM25 = doubleArrayOf(0.0, 13.0, 35.4, 55.4, 150.4, 250.4, 350.4, 500.4)
    val IAQI = doubleArrayOf(0.0, 50.0, 100.0, 150.0, 200.0, 300.0, 400.0, 500.0)
    var PM25CN = doubleArrayOf(0.0, 35.0, 75.0, 115.0, 150.0, 250.0, 350.0, 500.0)

    fun getAQIFromPM25(pm25: Double): Int {
        when {
            pm25 == 0.0 -> return 0
            pm25 < 0 -> return -1
            pm25 > 500 -> return 500
        }

        var i: Int = 0
        while (PM25[i] < pm25) {
            i += 1
        }

        val clow: Double = PM25[i - 1]
        val chigh: Double = PM25[i]
        val ilow: Double = IAQI[i - 1]
        val ihigh: Double = IAQI[i]


        val aqi = ((ihigh - ilow) / (chigh - clow) * (pm25 - clow)) + ilow
        return aqi.toInt()

    }

    fun getAQICNFromPM25(pm25: Double): Int {
        when {
            pm25 == 0.0 -> return 0
            pm25 < 0 -> return -1
            pm25 > 500 -> return 500
        }


        var i: Int = 0
        while (PM25CN[i] < pm25) i += 1

        val clow: Double = PM25CN[i - 1]
        val chigh: Double = PM25CN[i]
        val ilow: Double = IAQI[i - 1]
        val ihigh: Double = IAQI[i]


        val aqi = ((ihigh - ilow) / (chigh - clow) * (pm25 - clow)) + ilow
        return aqi.toInt()

    }

    fun getAQILevelFromPM25(pm25: Double): Int {
        when {
            pm25 == 0.0 -> return 0
            pm25 >= 500.0 -> return 7
        }

        var i: Int = 0
        while (PM25[i] < pm25) {
            i += 1
        }
        return i

    }

    fun getAQIStatusColorFromPM25(pm25: Double): UIColor {
        when {
            pm25 == 0.0 -> return UIColor.AQIGoodColor
            pm25 >= 500 -> return UIColor.AQIBeyondColor
        }

        var i: Int = 0
        while (PM25[i] < pm25) i += 1


        when (i) {
            0, 1 ->
                return UIColor.AQIGoodColor
            2 ->
                return UIColor.AQIModerateColor
            3 ->
                return UIColor.AQIGUnhealthyColor
            4 ->
                return UIColor.AQIUnhealthyColor
            5 ->
                return UIColor.AQIVUnhealthyColor
            6, 7 ->
                return UIColor.AQIHazardousColor
            else ->
                return UIColor.AQIHazardousColor
        }

    }

    //heat map
    fun getAQIWeightFromPM25(pm25: Double): Int {
        when {
            pm25 == 0.0 -> return 1
            pm25 >= 500 -> return 7
        }

        var i: Int = 0
        while (PM25[i] < pm25) i += 1

        return when (i) {
            0, 1 -> 1
            2 -> 2
            3 -> 3
            4 -> 4
            5 -> 5
            6, 7 -> 6
            else -> 7
        }

    }

    fun getAQITextColorFromPM25(pm25: Double): UIColor {
        when {
            pm25 == 0.0 -> return UIColor.AQIDarkTextColor
            pm25 >= 500 -> return UIColor.AQILightTextColor
        }

        var i: Int = 0
        while (PM25[i] < pm25) i += 1

        when (i) {
            0 ->
                return UIColor.AQIDarkTextColor
            1 ->
                return UIColor.AQIDarkTextColor
            2 ->
                return UIColor.AQIDarkTextColor
            3 ->
                return UIColor.AQIDarkTextColor
            4 ->
                return UIColor.AQILightTextColor
            5 ->
                return UIColor.AQILightTextColor
            6 ->
                return UIColor.AQILightTextColor
            7 ->
                return UIColor.AQILightTextColor
            else ->
                return UIColor.AQILightTextColor

        }

    }

    fun getAQIStatusTextFromPM25(pm25: Double): ResourceCommentWrapper {
        if (pm25 == 0.0) {
            return ResourceCommentWrapper(
                R.string.condition_good,
                commentRes = R.string.pm2_5_lvl_comment
            )
        }

        if (pm25 >= 500) {
            return ResourceCommentWrapper(
                R.string.condition_beyond_rating,
                commentRes = R.string.pm2_5_lvl_comment
            )
        }

        var i: Int = 0
        while (PM25[i] < pm25) i += 1

        when (i) {
            0 ->
                return ResourceCommentWrapper(
                    R.string.condition_good, commentRes = R.string.pm2_5_lvl_aqi_us_comment
                )
            1
            ->
                return ResourceCommentWrapper(
                    R.string.condition_good, commentRes = R.string.pm2_5_lvl_aqi_us_comment
                )
            2
            ->
                return ResourceCommentWrapper(
                    R.string.condition_moderate, commentRes = R.string.pm2_5_lvl_aqi_us_comment
                )
            3
            ->
                return ResourceCommentWrapper(
                    R.string.condition_unhealthy_sensitive_groups,
                    commentRes = R.string.condition_unhealthy_sensitive_groups_comment
                )
            4
            ->
                return ResourceCommentWrapper(
                    R.string.condition_unhealthy, commentRes = R.string.pm2_5_lvl_aqi_us_comment
                )
            5
            ->
                return ResourceCommentWrapper(
                    R.string.condition_very_unhealthy,
                    commentRes = R.string.pm2_5_lvl_aqi_us_comment
                )
            6
            ->
                return ResourceCommentWrapper(
                    R.string.condition_hazardous, commentRes = R.string.pm2_5_lvl_aqi_us_comment
                )
            7
            ->
                return ResourceCommentWrapper(
                    R.string.condition_hazardous, commentRes = R.string.pm2_5_lvl_aqi_us_comment
                )
            else ->
                return ResourceCommentWrapper(
                    R.string.condition_beyond_rating, commentRes =
                    R.string.pm2_5_lvl_aqi_us_comment
                )
        }
    }


    fun getAQICNTextColorFromPM25(pm25: Double): UIColor {
        when {
            pm25 == 0.0 -> return UIColor.AQIGoodColor
            pm25 >= 500 -> return UIColor.AQIBeyondColor
        }

        var i: Int = 0
        while (PM25CN[i] < pm25) i += 1

        when (i) {
            0 ->
                return UIColor.AQILightTextColor
            1 ->
                return UIColor.AQILightTextColor
            2 ->
                return UIColor.AQIDarkTextColor
            3 ->
                return UIColor.AQIDarkTextColor
            4 ->
                return UIColor.AQILightTextColor
            5 ->
                return UIColor.AQILightTextColor
            6 ->
                return UIColor.AQILightTextColor
            7 ->
                return UIColor.AQILightTextColor
            else ->
                return UIColor.AQILightTextColor
        }
    }

    fun getAQICNStatusColorFromPM25(pm25: Double): UIColor {
        when {
            pm25 == 0.0 -> return UIColor.AQICNExcellentColor
            pm25 >= 500 -> return UIColor.AQIBeyondColor
        }


        var i: Int = 0
        while (PM25CN[i] < pm25) i += 1

        return when (i) {
            0 -> UIColor.AQIGoodColor
            1 -> UIColor.AQIGoodColor
            2 -> UIColor.AQIModerateColor
            3 -> UIColor.AQIGUnhealthyColor
            4 -> UIColor.AQIUnhealthyColor
            5 -> UIColor.AQIVUnhealthyColor
            6 -> UIColor.AQIVUnhealthyColor
            7 -> UIColor.AQIBeyondColor
            else -> UIColor.AQIBeyondColor
        }
    }

    fun getAQICNStatusTextFromPM25(pm25: Double): ResourceCommentWrapper {
        when {
            pm25 == 0.0 -> return ResourceCommentWrapper(
                R.string.condition_excellent,
                commentRes = R.string.pm2_5_lvl_aqi_cn_comment
            )
            pm25 >= 500 -> return ResourceCommentWrapper(R.string.condition_beyond_rating)
        }

        var i: Int = 0
        while (PM25CN[i] < pm25) i += 1

        when (i) {
            0 ->
                return ResourceCommentWrapper(
                    R.string.condition_excellent,
                    commentRes = R.string.pm2_5_lvl_aqi_cn_comment
                )
            1 ->
                return ResourceCommentWrapper(
                    R.string.condition_excellent,
                    commentRes = R.string.pm2_5_lvl_aqi_cn_comment
                )
            2 ->
                return ResourceCommentWrapper(
                    R.string.condition_good,
                    commentRes = R.string.pm2_5_lvl_aqi_cn_comment
                )
            3 ->
                return ResourceCommentWrapper(
                    R.string.condition_lightly,
                    commentRes = R.string.pm2_5_lvl_aqi_cn_comment
                )
            4 ->
                return ResourceCommentWrapper(
                    R.string.condition_moderate,
                    commentRes = R.string.pm2_5_lvl_aqi_cn_comment
                )
            5 ->
                return ResourceCommentWrapper(
                    R.string.condition_heavy,
                    commentRes = R.string.pm2_5_lvl_aqi_cn_comment
                )
            6 ->
                return ResourceCommentWrapper(
                    R.string.condition_severe,
                    commentRes = R.string.pm2_5_lvl_aqi_cn_comment
                )
            7 ->
                return ResourceCommentWrapper(
                    R.string.condition_beyond_rating,
                    commentRes = R.string.pm2_5_lvl_aqi_cn_comment
                )
            else ->
                return ResourceCommentWrapper(
                    R.string.condition_beyond_rating,
                    commentRes = R.string.pm2_5_lvl_aqi_cn_comment
                )
        }
    }

    fun getDiskResFromCO2(co2: Double): Int {
        return if (co2 == 0.0) {
            R.drawable.green_seekbar_thumb //beyond value
        } else if (co2 > 0 && co2 < 700) {
            R.drawable.green_seekbar_thumb
        } else if (co2 >= 700 && co2 < 1000) {
            R.drawable.yellow_seekbar_thumb
        } else {
            R.drawable.red_seekbar_thumb
        }
    }

    fun getDiskResFromVoc(voc: Double): Int {
        return if (voc == 0.0) {
            R.drawable.green_seekbar_thumb
        } else if (voc > 0 && voc < 0.55) {
            R.drawable.green_seekbar_thumb
        } else if (voc >= 0.55 && voc < 0.65) {
            R.drawable.yellow_seekbar_thumb
        } else {
            R.drawable.red_seekbar_thumb
        }

    }

    fun getDiskResFromTmp(temp: Double): Int {
        return if (temp < 17) {
            R.drawable.red_seekbar_thumb
        } else if (temp >= 17 && temp < 19) {
            R.drawable.yellow_seekbar_thumb
        } else if (temp >= 25 && temp < 27) {
            R.drawable.yellow_seekbar_thumb
        } else if (temp >= 27) {
            R.drawable.red_seekbar_thumb
        } else {
            R.drawable.green_seekbar_thumb
        }

    }

    fun getDiskResFromHumid(humid: Double): Int {
        return if (humid < 35) {
            R.drawable.red_seekbar_thumb
        } else if (humid >= 35 && humid < 45) {
            R.drawable.yellow_seekbar_thumb
        } else if (humid >= 65 && humid < 75) {
            R.drawable.yellow_seekbar_thumb
        } else if (humid >= 75) {
            R.drawable.red_seekbar_thumb
        } else {
            R.drawable.green_seekbar_thumb
        }

    }


    fun getColorResFromCO2(co2: Double): Int {
        return if (co2 == 0.0) {
            R.color.aqi_beyond //beyond value
        } else if (co2 > 0 && co2 < 700) {
            R.color.aqi_good
        } else if (co2 >= 700 && co2 < 1000) {
            R.color.aqi_moderate
        } else {
            R.color.aqi_hazardous
        }
    }

    fun getColorResFromVoc(voc: Double): Int {
        return if (voc == 0.0) {
            R.color.aqi_good
        } else if (voc > 0 && voc < 0.55) {
            R.color.aqi_good
        } else if (voc >= 0.55 && voc < 0.65) {
            R.color.aqi_moderate
        } else {
            R.color.aqi_hazardous
        }

    }

    fun getColorResFromTmp(temp: Double): Int {
        return if (temp < 17) {
            R.color.aqi_hazardous
        } else if (temp >= 17 && temp < 19) {
            R.color.aqi_moderate
        } else if (temp >= 25 && temp < 27) {
            R.color.aqi_moderate
        } else if (temp >= 27) {
            R.color.aqi_hazardous
        } else {
            R.color.aqi_good
        }

    }

    fun getColorResFromHumid(humid: Double): Int {
        return if (humid < 35) {
            R.color.aqi_hazardous
        } else if (humid >= 35 && humid < 45) {
            R.color.aqi_moderate
        } else if (humid >= 65 && humid < 75) {
            R.color.aqi_moderate
        } else if (humid >= 75) {
            R.color.aqi_hazardous
        } else {
            R.color.aqi_good
        }

    }


    /** NOTE THAT THE SIZE OF ARRAY RETURNED AND ORDER MATTERS **/
    fun getRecommendationsGivePm25(pm25: Double): Array<ResourceCommentWrapper> {
        val aqi = getAQIFromPM25(pm25)
        return when {
            aqi < 35 -> {

                arrayOf(
                    ResourceCommentWrapper(
                        resourceId = R.drawable.mask_off,
                        commentRes = R.string.no_mask
                    ), ResourceCommentWrapper(
                        resourceId = R.drawable.outdoors_on,
                        commentRes = R.string.outdoor_acts_suitable
                    ), ResourceCommentWrapper(
                        resourceId = R.drawable.windows_open,
                        commentRes = R.string.windoors_open
                    ), ResourceCommentWrapper(
                        resourceId = R.drawable.fan_off,
                        commentRes = R.string.air_purify_not_needed
                    )
                )


            }
            aqi > 180 -> {

                arrayOf(
                    ResourceCommentWrapper(
                        resourceId = R.drawable.mask_on,
                        commentRes = R.string.masks_recommended
                    ), ResourceCommentWrapper(
                        resourceId = R.drawable.outdoors_off,
                        commentRes = R.string.avoid_outdoors
                    ), ResourceCommentWrapper(
                        resourceId = R.drawable.windows_close,
                        commentRes = R.string.close_windows
                    ), ResourceCommentWrapper(
                        resourceId = R.drawable.fan_on,
                        commentRes = R.string.air_purify_needed
                    )
                )

            }
            else -> {

                arrayOf(
                    ResourceCommentWrapper(
                        resourceId = R.drawable.mask_on,
                        commentRes = R.string.masks_recommended_sensitive
                    ), ResourceCommentWrapper(
                        resourceId = R.drawable.outdoors_off,
                        commentRes = R.string.minimal_outdoor_acts
                    ), ResourceCommentWrapper(
                        resourceId = R.drawable.windows_close,
                        commentRes = R.string.close_windows
                    ), ResourceCommentWrapper(
                        resourceId = R.drawable.fan_on,
                        commentRes = R.string.air_purify_recommended
                    )
                )
            }
        }
    }


    /*********TODO remove after testing UNUSED from IOS
    fun getAQITextFromVOC(voc: Double): ConditionResStrings {
    return if (voc <= 0) {
    ConditionResStrings(
    R.string.condition_undetermined,
    commentRes = R.string.voc_lvl_comment
    )
    } else if (voc > 0 && voc < 0.55) {
    ConditionResStrings(R.string.condition_good, commentRes = R.string.good_voc_lvl_comment)
    } else if (voc >= 0.55 && voc < 0.65) {
    ConditionResStrings(
    R.string.condition_moderate,
    commentRes = R.string.moderate_voc_lvl_comment
    )
    } else {
    ConditionResStrings(
    R.string.condition_unhealthy,
    commentRes = R.string.unhealthy_voc_lvl_comment
    )
    }

    }
    fun getAQILevelFromCO2(co2: Double): Int {
    return if (co2 == 0.0) {
    0
    } else if (co2 > 0 && co2 < 700) {
    1
    } else if (co2 >= 700 && co2 < 1000) {
    2
    } else {
    3
    }
    }
    fun getAQITextFromCO2(co2: Double): ConditionResStrings {
    return if (co2 == 0.0) {
    ConditionResStrings(
    R.string.condition_undetermined,
    commentRes = R.string.co_2_lvl_comment
    )
    } else if (co2 > 0 && co2 < 700) {
    ConditionResStrings(R.string.condition_good, commentRes = R.string.co_2_lvl_comment)
    } else if (co2 >= 700 && co2 < 1000) {
    ConditionResStrings(R.string.condition_moderate, commentRes = R.string.co_2_lvl_comment)
    } else {
    ConditionResStrings(
    R.string.condition_unhealthy,
    commentRes = R.string.co_2_lvl_comment
    )
    }

    }
    fun getAQILevelFromVOC(voc: Double): Int {
    return if (voc <= 0) {
    0
    } else if (voc > 0 && voc < 0.55) {
    1
    } else if (voc >= 0.55 && voc < 0.65) {
    2
    } else {
    3
    }

    }


    /****using a slider thumb disc res gen instead  ***********/
    fun getAQIColorFromCO2(co2: Double): UIColor {
    return if (co2 == 0.0) {
    UIColor.AQIBeyondColor
    } else if (co2 > 0 && co2 < 700) {
    UIColor.AQIGoodColor
    } else if (co2 >= 700 && co2 < 1000) {
    UIColor.AQIModerateColor
    } else {
    UIColor.AQIUnhealthyColor
    }
    }
    fun getAQIColorFromHumid(humid: Double): UIColor {
    return if (humid < 35) {
    UIColor.AQIUnhealthyColor
    } else if (humid >= 35 && humid < 45) {
    UIColor.AQIModerateColor
    } else if (humid >= 65 && humid < 75) {
    UIColor.AQIModerateColor
    } else if (humid >= 75) {
    UIColor.AQIUnhealthyColor
    } else {
    UIColor.AQIGoodColor
    }

    }
    fun getAQIColorFromTemp(temp: Double): UIColor {
    return if (temp < 17) {
    UIColor.AQIUnhealthyColor
    } else if (temp >= 17 && temp < 19) {
    UIColor.AQIModerateColor
    } else if (temp >= 25 && temp < 27) {
    UIColor.AQIModerateColor
    } else if (temp >= 27) {
    UIColor.AQIUnhealthyColor
    } else {
    UIColor.AQIGoodColor
    }

    }
    fun getAQIColorFromVOC(voc: Double): UIColor {
    return if (voc == 0.0) {
    UIColor.AQIBeyondColor
    } else if (voc > 0 && voc < 0.55) {
    UIColor.AQIGoodColor
    } else if (voc >= 0.55 && voc < 0.65) {
    UIColor.AQIModerateColor
    } else {
    UIColor.AQIUnhealthyColor
    }

    } ****/


}


//generic class that ties a resource drawable/color/string value to an optional comment string
data class ResourceCommentWrapper(
    val resourceId: Int,
    val commentRes: Int? = null
)