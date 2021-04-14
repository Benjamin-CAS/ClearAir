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
                value = ["company_id", "location_id", "bound_to_scanned_device_id"],
                unique = true
        )]
)
@Parcelize
class LocationDetails(
        @PrimaryKey(autoGenerate = true)
        val autoId: Int,
        var company_id: String = "",
        var location_id: String = "",
        var lastUpdated: Long = System.currentTimeMillis(),
        @Embedded val indoor: Indoor,
        @Embedded val outdoor: Outdoor,
        @Embedded val energy: Energy,
        val ExpFilter: String? = "",
        val ExpEquip: String? = "",
        var lastKnownUserName: String = "", //for refreshing
        var lastKnownPassword: String = "", //for refreshing
        var is_mine: Boolean = true,
        var bound_to_scanned_device_id: String //mapped as compIdLocIdMonitorId --of LocationDataFromQr
) : Parcelable {
    fun getFormattedUpdateTime(): String {
        val date = Date(lastUpdated)
        val dateFormat: DateFormat = SimpleDateFormat("dd - MMMM HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }

        fun deConstructDeviceIdBoundedTo(): Triple<String, String, String> {
               val compIdLocId = "$company_id$location_id"
               val monitorId = bound_to_scanned_device_id.substringAfter(compIdLocId)
               return Triple(company_id, location_id, monitorId)
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
data class LocationDetailsGeneralDataWrapper(
        val locationDetails: LocationDetails,
        val generalDataFromQr: LocationDataFromQr
) : Parcelable


fun createDeviceIdToBindTo(compId: String, locId: String, monitorId: String): String {
        //to be used as  bound_to_scanned_device_id mapped as compIdLocIdMonitorId --of LocationDataFromQr
        return "$compId$locId$monitorId"
}