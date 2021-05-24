package com.android_dev.cleanairspaces.persistence.local.models.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "devices_data")
@Parcelize
data class DevicesDetails(
    val watch_device: Boolean = false,
    val id: String,
    val device_type: String,
    val dev_name: String,
    val mac: String,
    val mode: String,
    val fan_speed: String,
    val df: String,
    val iforce: String,
    val fa: String,
    val last_mode: String,
    val last_fan: String,
    val last_fa: String,
    val last_voc: String,
    val last_time: String,
    val status: String, //Med, High, Low
    @PrimaryKey(autoGenerate = false)
    var actualDataTag: String = "", //comp_id+loc_id+monitor_id+device_id
    var lastRecUname: String = "",
    var lastRecPwd: String = "",
    var for_watched_location_tag: String,
    val updated_on: Long = System.currentTimeMillis()
) : Parcelable {
    fun isOn(): Boolean {
        return true
    }
}
