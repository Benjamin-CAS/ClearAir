package com.android_dev.cleanairspaces.persistence.local.models.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import com.amap.api.maps.model.LatLng as ALatLng
import com.google.android.gms.maps.model.LatLng as GLatLng


@Parcelize
@Entity(
        tableName = "map_displayed_data",
        indices = [androidx.room.Index(
                value = ["lat", "lon"],
                unique = true
        )]
)
data class MapData(
        @PrimaryKey(autoGenerate = false)
        val actualDataTag: String,

        val lat: Double,
        val lon: Double,

        val pm25: Double,

        val last_updated: Long = System.currentTimeMillis()
) : Parcelable {
    fun getAMapLocationLatLng() = ALatLng(lat, lon)
    fun getGMapLocationLatLng(): GLatLng = GLatLng(lat, lon)
}