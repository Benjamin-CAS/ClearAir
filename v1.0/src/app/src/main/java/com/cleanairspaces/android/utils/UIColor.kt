package com.cleanairspaces.android.utils

import android.graphics.Color


enum class UIColor(val red: Double, val green: Double, val blue: Double) {

    CASGreenColor(red = 0.0, green = 1.0, blue = 0.2),

    CASDarkGreenColor(red = 0.2, green = 0.7, blue = 0.4),

    CASLightGreenColor(red = 0.55, green = 0.8, blue = 0.45),

    CASBlueColor(red = 0.18, green = 0.26, blue = 0.53),

    CASYellowColor(red = 0.9, green = 0.8, blue = 0.0),

    CASRedColor(red = 0.9, green = 0.1, blue = 0.15),

    CASOrangeColor(red = 0.9, green = 0.4, blue = 0.25),

    CASFooterColor(red = 0.85, green = 0.85, blue = 0.9),

    CASLightBlueLabel(red = 0.6, green = 0.8, blue = 0.9),

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

        return Color.argb(70, red, green, blue)
    }


}