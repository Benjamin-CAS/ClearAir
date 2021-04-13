package com.cleanairspaces.android.utils

import android.graphics.Color
import com.cleanairspaces.android.R


enum class UIColor(val red: Double, val green: Double, val blue: Double) {

    // AIR QUALITY COLORS
    AQIGoodColor(red = 0.2, green = 0.7, blue = 0.4),

    AQIModerateColor(red = 0.9, green = 0.85, blue = 0.0),

    AQIGUnhealthyColor(red = 1.0, green = 0.6, blue = 0.0),

    AQIUnhealthyColor(red = 1.0, green = 0.0, blue = 0.0),

    AQIVUnhealthyColor(red = 0.7, green = 0.0, blue = 0.7),

    AQIHazardousColor(red = 0.8, green = 0.1, blue = 0.1),

    AQIBeyondColor(red = 0.8, green = 0.8, blue = 0.8),


    AQIDarkTextColor(red = 0.0, green = 0.0, blue = 0.0),

    AQILightTextColor(red = 0.95, green = 0.95, blue = 0.95),

    AQICNExcellentColor(red = 0.2, green = 0.4, blue = 0.9),

}

object MyColorUtils {

    fun convertUIColorToRGB(uiColor: UIColor): Int {

        val red = (uiColor.red * 255).toInt()
        val green = (uiColor.green * 255).toInt()
        val blue = (uiColor.blue * 255).toInt()

        return Color.rgb(red, green, blue)
    }

    fun getGradientColors(): IntArray {
        return intArrayOf(
                convertUIColorToRGB(UIColor.AQIGoodColor),
                convertUIColorToRGB(UIColor.AQIModerateColor),
                convertUIColorToRGB(UIColor.AQIGUnhealthyColor),
                convertUIColorToRGB(UIColor.AQIUnhealthyColor),
                convertUIColorToRGB(UIColor.AQIVUnhealthyColor),
                convertUIColorToRGB(UIColor.AQIHazardousColor),
                convertUIColorToRGB(UIColor.AQIBeyondColor)
        )
    }

    fun getGradientIntensities(): FloatArray {
        //7 is 100 percent --- we have  1 to 7
        return floatArrayOf(0.14f, 0.29f, 0.43f, 0.57f, 0.71f, 0.86f, 1f)
    }

    fun convertUIColorToStatusRes(statusColor: UIColor): Int {
        return when (statusColor) {
            UIColor.AQIGoodColor -> {
                R.drawable.aqi_good_status
            }
            UIColor.AQIModerateColor -> {
                R.drawable.aqi_moderate_status
            }
            UIColor.AQIGUnhealthyColor -> {
                R.drawable.aqi_g_unhealthy_status
            }
            UIColor.AQIUnhealthyColor -> {
                R.drawable.aqi_unhealthy_status
            }
            UIColor.AQIVUnhealthyColor -> {
                R.drawable.aqi_v_unhealthy_status
            }
            UIColor.AQIHazardousColor -> {
                R.drawable.aqi_hazardous_status
            }
            else -> {
                //beyond
                R.drawable.aqi_beyond_status
            }
        }
    }

    fun convertUIColorToColorRes(statusColor: UIColor): Int {
        return when (statusColor) {
            UIColor.AQIGoodColor -> {
                R.color.aqi_good
            }
            UIColor.AQIModerateColor -> {
                R.color.aqi_moderate
            }
            UIColor.AQIGUnhealthyColor -> {
                R.color.aqi_g_unhealthy
            }
            UIColor.AQIUnhealthyColor -> {
                R.color.aqi_unhealthy
            }
            UIColor.AQIVUnhealthyColor -> {
                R.color.aqi_v_unhealthy
            }
            UIColor.AQIHazardousColor -> {
                R.color.aqi_hazardous
            }
            else -> {
                //beyond
                R.color.aqi_beyond
            }
        }
    }


}