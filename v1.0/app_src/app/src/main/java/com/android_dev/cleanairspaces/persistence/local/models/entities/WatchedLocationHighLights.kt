package com.android_dev.cleanairspaces.persistence.local.models.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android_dev.cleanairspaces.utils.BASE_URL
import kotlinx.parcelize.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
@Entity(tableName = "watched_location")
data class WatchedLocationHighLights(
        @PrimaryKey(autoGenerate = false)
        val actualDataTag: String,

        val lat: Double,
        val lon: Double,

        val pm_indoor: Double?,
        val pm_outdoor: Double?,

        val name: String,
        val compId: String,
        val locId: String,
        val lastRecPwd: String,
        val lastRecUsername: String,
        val logo: String,

        var indoor_co2: Double?,
        var indoor_voc: Double?,
        var indoor_temperature: Double?,
        var indoor_humidity: Double?,

        val location_area: String,

        val energyMax: Double?,
        val energyMonth: Double?,

        val isIndoorLoc: Boolean,

        val last_updated: Long = System.currentTimeMillis(),
        val monitorId: String,

        var is_secure: Boolean,

) : Parcelable {

    fun getFullLogoUrl(): String {
        return if (logo.isEmpty()) ""
        else LOGO_BASE_URL + logo
    }

    fun getUpdatedOnFormatted(): String {
        val date = Date(this.last_updated)
        val dateFormat: DateFormat = SimpleDateFormat("dd - MMMM HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }

    companion object {
        private const val LOGO_BASE_URL = "${BASE_URL}assets/images/logo/"
    }
}