package com.cleanairspaces.android.models.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "my_location_details",
    indices = [androidx.room.Index(
        value = ["company_id", "location_id"],
        unique = true
    )]
)
@Parcelize
class MyLocationDetails(
    @PrimaryKey(autoGenerate = true)
    val autoId: Long,
    var company_id: String = "",
    var location_id: String = "",
    var lastUpdated: Long = System.currentTimeMillis(),
    @Embedded val indoor: Indoor,
    @Embedded val outdoor: Outdoor,
    @Embedded val energy: Energy,
    val ExpFilter: String? = "",
    val ExpEquip: String? = ""
) : Parcelable {
    fun getFormattedUpdateTime(): String {
        val date = Date(lastUpdated)
        val dateFormat: DateFormat = SimpleDateFormat("dd - MM HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }
}

@Parcelize
data class Indoor(
    val indoor_pm: String = "",
    val indoor_co2: String = "",
    val indoor_voc: String = "",
    val indoor_temperature: String = "",
    val indoor_humidity: String = "",
    val indoor_time: String? = "",
    val param_label: String? = "",
    val display_param: String? = "",
    @ColumnInfo(name = "indoor_name_en")
    val name_en: String? = "",
    @ColumnInfo(name = "indoor_lon")
    val lon: String? = "",
    @ColumnInfo(name = "indoor_lat")
    val lat: String? = "",
) : Parcelable

@Parcelize
data class Outdoor(
    val outdoor_pm: String = "",
    val outdoor_time: String? = "",
    val outdoor_display_param: String? = "",
    @ColumnInfo(name = "outdoor_name_en")
    val name_en: String? = "",
    @ColumnInfo(name = "outdoor_lon")
    val lon: String? = "",
    @ColumnInfo(name = "outdoor_lat")
    val lat: String? = "",
) : Parcelable

@Parcelize
data class Energy(
    val month: String? = "",
    val max: String? = "",
    val current_used: String? = "",
    val current_max: String? = "",
) : Parcelable


// for UI purposes only
@Parcelize
data class CustomerDeviceDataDetailed(
    val locationDetails: MyLocationDetails,
    val deviceData: CustomerDeviceData
) : Parcelable