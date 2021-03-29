package com.cleanairspaces.android.models.entities

import android.os.Parcelable
import com.amap.api.maps2d.model.LatLng
import com.cleanairspaces.android.R
import kotlinx.parcelize.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class LocationInfo(
        val location_name: String,
        var updated: Long,
        var location_area: String,
        var indoor_pm: String,
        var indoor_points: Int,
        var indoor_status: LocationStatus,
        var outdoor_pm: String,
        var outdoor_points: Int,
        var outdoor_status: LocationStatus,
        var location: LatLng,
        var locationLogoRes: Int = R.drawable.clean_air_spaces_logo_name,
):Parcelable{
    fun getFormattedUpdateTime(): String {
        val date = Date(updated)
        val dateFormat: DateFormat = SimpleDateFormat("dd - MM HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }
    override fun toString(): String {
        return "$location_name at ${location.latitude},${location.longitude} is ${indoor_status.name} indoors and ${outdoor_status.name} outdoors"
    }
}


enum class LocationStatus(val colorRes: Int, val drawableRes: Int, val strRes: Int, val statusIndicatorRes: Int){
    DANGER(colorRes = R.color.transparent_orange, drawableRes = R.drawable.danger_location_marker, strRes = R.string.danger_txt, statusIndicatorRes = R.drawable.danger_status_indicator),
    SAFE(colorRes = R.color.transparent_green, drawableRes = R.drawable.safe_location_marker, strRes = R.string.good_air_status_txt, statusIndicatorRes = R.drawable.good_status_indicator),
    MODERATE(colorRes = R.color.transparent_yellow, drawableRes = R.drawable.moderate_location_marker, strRes = R.string.moderate_air_status_txt, statusIndicatorRes = R.drawable.moderate_status_indicator)
}
