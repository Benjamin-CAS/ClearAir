package com.cleanairspaces.android.models.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cleanairspaces.android.utils.DEFAULT_LOCATION_EN_NAME
import kotlinx.parcelize.Parcelize
import com.google.android.gms.maps.model.LatLng as gMapLatLng
import  com.amap.api.maps.model.LatLng as aMapLatLng

@Parcelize
@Entity(
    tableName = "out_door_locations",
    indices = [Index(value = ["lat", "lon"], unique = true)]
)
data class OutDoorLocations(
    @PrimaryKey(autoGenerate = true)
    val autoId: Int = 0,
    val location_id: String = "",
    val monitor_id: String = "",
    val name_en: String = DEFAULT_LOCATION_EN_NAME,
    val reading: String = "",
    val date_reading: String = "",
    val pm2p5: String = "",
    val lon: String,
    val lat: String,
    val location_area: LocationAreas = LocationAreas.OTHER
) : Parcelable {
    fun getGMapLocationLatLng(): gMapLatLng {
        return gMapLatLng(lat.toDouble(), lon.toDouble())
    }

    fun getAMapLocationLatLng(): aMapLatLng {
        return aMapLatLng(lat.toDouble(), lon.toDouble())
    }

    fun getLocationNameEn(): String {
        return if (name_en == DEFAULT_LOCATION_EN_NAME) {
            when (location_area) {
                LocationAreas.AMERICA -> "In ${location_area.areaName}"
                LocationAreas.TAIWAN -> "In ${location_area.areaName}"
                else -> "Location"
            }
        } else
            name_en
    }
}

enum class LocationAreas(val areaName: String) {
    AMERICA(areaName = "america"),
    TAIWAN(areaName = "taiwan"),
    OTHER(areaName = "other")
}