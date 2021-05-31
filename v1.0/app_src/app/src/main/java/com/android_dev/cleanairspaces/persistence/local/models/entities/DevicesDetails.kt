package com.android_dev.cleanairspaces.persistence.local.models.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android_dev.cleanairspaces.R
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = "devices_data")
@Parcelize
data class DevicesDetails(
    var watch_device: Boolean = false,
    val id: String,
    val device_type: String,
    val dev_name: String,
    val mac: String,
    val mode: String, // Manual - 1 or Automatic - 2
    val fan_speed: String, // Off - 0 Low - 1 Med -2 High - 3 On - 1
    val df: String, // On - 1  or Off - 0
    val iforce: String,
    val fa: String, //On - 1 or Off - 0
    val last_mode: String,
    val last_fan: String,
    val last_fa: String,
    val last_voc: String,
    val last_time: String,
    val status: String, //off on dis paused low med high uv issues refreshing...
    @PrimaryKey(autoGenerate = false)
    var actualDataTag: String = "", //comp_id+loc_id+monitor_id+device_id
    var compId: String,
    var locId: String,
    var lastRecUname: String = "",
    var lastRecPwd: String = "",
    var for_watched_location_tag: String,
    val updated_on: Long = System.currentTimeMillis()
) : Parcelable {
    fun isModeAuto(): Boolean {
        return mode.trim().toLowerCase(Locale.ENGLISH) == AUTO
    }

    fun getStatusColor(): DeviceDetailsStatus {
        return when (status.trim().toLowerCase(Locale.ENGLISH)) {
            IS_OFF -> DeviceDetailsStatus.OFF
            IS_ON -> DeviceDetailsStatus.ON
            IS_PAUSED -> DeviceDetailsStatus.PAUSED
            IS_DISCONNECTED -> DeviceDetailsStatus.DISCONNECTED
            IS_UV_OFF -> DeviceDetailsStatus.UV_ISSUES
            IS_LOW -> DeviceDetailsStatus.ON_LOW
            IS_MEDIUM -> DeviceDetailsStatus.ON_MED
            IS_HIGH -> DeviceDetailsStatus.ON_HIGH
            IS_RESTARTING -> DeviceDetailsStatus.RESTARTING
            else -> DeviceDetailsStatus.REFRESHING
        }
    }

    fun isDuctFitOn(): Boolean {
        return df.trim().toLowerCase(Locale.ENGLISH) == ON_STATUS
    }

    fun isFreshAirOn(): Boolean {
        return fa.trim().toLowerCase(Locale.ENGLISH) == OFF_STATUS
    }

    fun isFanOn(): Boolean {
        return fan_speed.trim().toLowerCase(Locale.ENGLISH) != OFF_STATUS
    }

    fun isFanHigh(): Boolean {
        return fan_speed.trim().toLowerCase(Locale.ENGLISH) == HIGH_SPEED
    }

    fun isFanMed(): Boolean {
        return fan_speed.trim().toLowerCase(Locale.ENGLISH) == MED_SPEED
    }

    fun isFanLow(): Boolean {
        return fan_speed.trim().toLowerCase(Locale.ENGLISH) == LOW_SPEED
    }


    fun isTurboFanOn(): Boolean {
        return fan_speed.trim().toLowerCase(Locale.ENGLISH) != OFF_STATUS
    }

    fun isTurboFanTurbo(): Boolean {
        return fan_speed.trim().toLowerCase(Locale.ENGLISH) == TURBO_FULL_SPEED
    }

    fun isTurboFanHigh(): Boolean {
        return fan_speed.trim().toLowerCase(Locale.ENGLISH) == TURBO_HIGH_SPEED
    }

    fun isTurboFanMed(): Boolean {
        return fan_speed.trim().toLowerCase(Locale.ENGLISH) == TURBO_MED_SPEED
    }

    fun isTurboFanLow(): Boolean {
        return fan_speed.trim().toLowerCase(Locale.ENGLISH) == TURBO_LOW_SPEED
    }

    fun isTurboFanSleep(): Boolean {
        return fan_speed.trim().toLowerCase(Locale.ENGLISH) == TURBO_SLEEP_SPEED
    }

    override fun toString(): String {
        return "watch_device  = $watch_device id  = $id device_type  = $device_type dev_name  = $dev_name mac  = $mac  mode  = $mode  fan_speed  = $fan_speed df  = $df iforce  = $iforce fa  = $fa last_mode  = $last_mode  last_fan  = $last_fan  last_fa  = $last_fa  last_voc  = $last_voc  last_time  = $last_time  status  = $status  actualDataTag  = $actualDataTag  compId  = $compId  locId  = $locId  lastRecUname  = $lastRecUname  lastRecPwd  = $lastRecPwd  for_watched_location_tag  = $for_watched_location_tag "
    }
}

enum class DeviceDetailsStatus(val statusTxt: Int, val colorRes: Int) {
    OFF(statusTxt = R.string.status_off, colorRes = R.color.cas_blue),
    ON(statusTxt = R.string.status_on, colorRes = R.color.background_good),
    PAUSED(statusTxt = R.string.status_paused, colorRes = R.color.cas_blue),
    DISCONNECTED(statusTxt = R.string.status_disconnected, colorRes = R.color.background_bad),
    REFRESHING(statusTxt = R.string.status_refreshing, colorRes = R.color.refresh),
    RESTARTING(statusTxt = R.string.status_restarting, colorRes = R.color.dark_grey),
    ON_LOW(statusTxt = R.string.status_low, colorRes = R.color.background_good),
    ON_MED(statusTxt = R.string.status_med, colorRes = R.color.background_good),
    ON_HIGH(statusTxt = R.string.status_high, colorRes = R.color.background_good),
    UV_ISSUES(
        statusTxt = R.string.device_status_uv_issues_display_lbl,
        colorRes = R.color.dark_grey
    )
}

const val LOW_SPEED = "1"
const val MED_SPEED = "2"
const val HIGH_SPEED = "3"
const val OFF_STATUS = "0"
const val ON_STATUS = "1"
const val AUTO = "2"
const val MANUAL = "1"
const val REFRESHING = "refreshing..."
const val IS_PAUSED = "paused"
const val IS_DISCONNECTED = "dis"
const val IS_UV_OFF = "uv issues"
const val IS_RESTARTING = "restarting"
const val IS_LOW = "low"
const val IS_MEDIUM = "med"
const val IS_HIGH = "high"
const val IS_ON = "on"
const val IS_OFF = "off"
const val TURBO_SLEEP_SPEED = "1"
const val TURBO_LOW_SPEED = "2"
const val TURBO_MED_SPEED = "3"
const val TURBO_HIGH_SPEED = "4"
const val TURBO_FULL_SPEED = "5"