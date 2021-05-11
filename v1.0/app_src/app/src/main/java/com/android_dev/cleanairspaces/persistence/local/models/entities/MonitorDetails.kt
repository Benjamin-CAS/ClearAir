package com.android_dev.cleanairspaces.persistence.local.models.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
@Entity(
        tableName = "monitors_data",
        indices = [androidx.room.Index(
                value = ["monitor_id"],
                unique = true
        )]
)
data class MonitorDetails(
        @PrimaryKey(autoGenerate = false)
        val actualDataTag: String, //comp_id+loc_id+monitor_id
        val for_watched_location_tag : String,
        val company_id : String,
        val location_id : String,
        val monitor_id : String,
        val lastRecUName : String,
        val lastRecPwd : String,
        val indoor_name_en: String,
        val indoor_pm_25: Double?,
        val indoor_tvoc: Double?,
        val indoor_co2: Double?,
        val indoor_temperature: Double?,
        val indoor_humidity: Double?,
        val indoor_display_param: String?,
        val outdoor_pm: Double?,
        val outdoor_display_param: String?,
        val outdoor_name_en: String?,
        val updated_on : Long = System.currentTimeMillis(),
        var watch_location : Boolean = false
):Parcelable {
        fun getUpdatedOnFormatted(): String {
                val date = Date(this.updated_on)
                val dateFormat: DateFormat = SimpleDateFormat("dd - MMMM HH:mm", Locale.getDefault())
                return dateFormat.format(date)
        }
}