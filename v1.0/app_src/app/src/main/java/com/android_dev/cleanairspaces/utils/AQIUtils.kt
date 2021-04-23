package com.android_dev.cleanairspaces.utils

import com.android_dev.cleanairspaces.R


/**** from range of 1 to 6, 1 being good, six being hazardous *********/
private val pm25UpperLimits = arrayOf<Double>(
        12.0,
        35.4,
        55.4,
        150.4,
        250.4,
        350.4,
        500.0,
)
private val pm25UpperLimitsCn = arrayOf<Double>(35.0, 75.0, 115.0, 150.0, 250.0, 500.0)
private val aqiUpperLimitsCn = arrayOf<Double>(50.0, 100.0, 150.0, 200.0, 300.0, 500.0)


fun getAQIStatusFromPM25(pm25: Double, aqiIndex: String = DEFAULT_AQI_INDEX_PM25): AQIStatus {
    val scaleToUse = if (aqiIndex == DEFAULT_AQI_INDEX_PM25 ||
            aqiIndex == AQI_INDEX_AQI_US
    ) {
        pm25UpperLimits
    } else {
        pm25UpperLimitsCn
    }
    return when {
        (pm25 <= scaleToUse[0]) -> AQIStatus.GOOD
        (pm25 <= scaleToUse[1]) -> AQIStatus.MODERATE
        (pm25 <= scaleToUse[2]) -> AQIStatus.GUnhealthyColor
        (pm25 <= scaleToUse[3]) -> AQIStatus.UnhealthyColor
        (pm25 <= scaleToUse[4]) -> AQIStatus.VUnhealthyColor
        else -> AQIStatus.HazardousColor
    }
}

fun getRecommendationsGivenAQIColorRes(aqiColorRes: Int): Array<ResourceCommentWrapper> {
    return when (aqiColorRes) {
        R.color.aqi_good -> {

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
        R.color.aqi_moderate,
        R.color.aqi_g_unhealthy -> {

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
        else -> {
            arrayOf(
                    ResourceCommentWrapper(
                            resourceId = R.drawable.mask_on_necessary,
                            commentRes = R.string.masks_recommended
                    ), ResourceCommentWrapper(
                    resourceId = R.drawable.outdoors_off_necessary,
                    commentRes = R.string.avoid_outdoors
            ), ResourceCommentWrapper(
                    resourceId = R.drawable.windows_close_necessary,
                    commentRes = R.string.close_windows
            ), ResourceCommentWrapper(
                    resourceId = R.drawable.fan_on_necessary,
                    commentRes = R.string.air_purify_needed
            )
            )

        }
    }
}


enum class AQIStatus(
        val level_intensity: Double,
        val aqi_color_res: Int,
        val status_bar_res: Int,
        val lbl: Int,
        val transparentRes: Int,
        val diskRes: Int,
        val transparentCircleRes: Int
) {
    GOOD(
            level_intensity = 1.0,
            aqi_color_res = R.color.aqi_good,
            status_bar_res = R.drawable.aqi_good_status,
            lbl = R.string.good_air_status_txt,
            transparentRes = R.color.transparent_green,
            diskRes = R.drawable.green_seekbar_thumb,
            transparentCircleRes = R.drawable.good_circle
    ),
    MODERATE(
            level_intensity = 2.0,
            aqi_color_res = R.color.aqi_moderate,
            status_bar_res = R.drawable.aqi_moderate_status,
            lbl = R.string.moderate_air_status_txt,
            transparentRes = R.color.transparent_yellow,
            diskRes = R.drawable.yellow_seekbar_thumb,
            transparentCircleRes = R.drawable.moderate_circle
    ),
    GUnhealthyColor(
            level_intensity = 3.0,
            aqi_color_res = R.color.aqi_g_unhealthy,
            status_bar_res = R.drawable.aqi_g_unhealthy_status,
            lbl = R.string.aqi_status_unhealthy_sensitive_groups,
            transparentRes = R.color.transparent_orange,
            diskRes = R.drawable.yellow_seekbar_thumb,
            transparentCircleRes = R.drawable.g_unhealthy_circle
    ),
    UnhealthyColor(
            level_intensity = 4.0,
            aqi_color_res = R.color.aqi_unhealthy,
            status_bar_res = R.drawable.aqi_unhealthy_status,
            lbl = R.string.aqi_status_unhealthy,
            transparentRes = R.color.transparent_red,
            diskRes = R.drawable.red_seekbar_thumb,
            transparentCircleRes = R.drawable.unhealthy_circle
    ),
    VUnhealthyColor(
            level_intensity = 5.0,
            aqi_color_res = R.color.aqi_v_unhealthy,
            status_bar_res = R.drawable.aqi_v_unhealthy_status,
            lbl = R.string.aqi_status_very_unhealthy,
            transparentRes = R.color.transparent_red,
            diskRes = R.drawable.red_seekbar_thumb,
            transparentCircleRes = R.drawable.v_unhealthy_circle
    ),
    HazardousColor(
            level_intensity = 6.0,
            R.color.aqi_hazardous,
            status_bar_res = R.drawable.aqi_hazardous_status,
            R.string.aqi_status_hazardous,
            transparentRes = R.color.transparent_red,
            diskRes = R.drawable.red_seekbar_thumb,
            transparentCircleRes = R.drawable.hazardous_circle
    ),
}

/********************** pm TEMP, CO2 & HUMID sliders *****/
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

fun getCO2LvlIn100Scale(co2: Double): Int {
    return if (co2 == 0.0) {
        0
    } else if (co2 > 0 && co2 < 700) {
        /* max here is 30 */
        val portion = 700 / 3
        if (co2 < portion) 5 else if (co2 > (portion * 2)) 25 else 15
    } else if (co2 >= 700 && co2 < 1000) {
        /* max here is 60, min is 30 */
        val portion = 1000 / 3
        if (co2 < portion) 35 else if (co2 > (portion * 2)) 55 else 45
    } else {
        90
    }
}


fun getDiskResFromTVoc(tvoc: Double): Int {
    return if (tvoc == 0.0) {
        R.drawable.green_seekbar_thumb
    } else if (tvoc > 0 && tvoc < 0.55) {
        R.drawable.green_seekbar_thumb
    } else if (tvoc >= 0.55 && tvoc < 0.65) {
        R.drawable.yellow_seekbar_thumb
    } else {
        R.drawable.red_seekbar_thumb
    }
}

fun getTVocLvLIn100Scale(tvoc: Double): Int {
    return if (tvoc == 0.0) {
        0
    } else if (tvoc > 0 && tvoc < 0.55) {
        val portion = 0.55 / 3
        if (tvoc < portion) 5 else if (tvoc > (portion * 2)) 25 else 15
    } else if (tvoc >= 0.55 && tvoc < 0.65) {
        val portion = 0.65 / 3
        if (tvoc < portion) 35 else if (tvoc > (portion * 2)) 55 else 45
    } else {
        90
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

//generic class that ties a resource drawable/color/string value to an optional comment string
data class ResourceCommentWrapper(
        val resourceId: Int,
        val commentRes: Int? = null
)