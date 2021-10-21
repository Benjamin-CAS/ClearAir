package com.android_dev.cleanairspaces.persistence.local.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android_dev.cleanairspaces.R

/**
 * @author Benjamin
 * @description:
 * @date :2021.10.14 9:42
 */
@Entity(tableName = "air_conditioner")
data class AirConditionerEntity(
    val auto: String,
    val current: String,
    val devName: String,
    val deviceType: String,
    val fanSpeed: String,
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val iforce: String,
    val lastAuto: String,
    val lastFan: String,
    val lastMode: String,
    val lastTime: String,
    val locationId: String,
    val mac: String,
    val mode: String,
    val nameEn: String,
    val status: Int,
    val tCal: String,
    var target: String,
    val version: String,
    val zoneId: String,
    val zoneMode: String,
    var watchAirConditioner: Boolean = false,
){
    fun zoneModeSelect() = when(zoneMode){
        "1" -> R.id.independent_btn
        "2" -> R.id.inside_zone_btn
        else -> null
    }
    fun lastModeSelect() = when(lastMode){
        "0" -> R.id.fan_btn
        "1" -> R.id.cooling_btn
        "2" -> R.id.heating_btn
        "3" -> R.id.auto_btn
        else -> null
    }
    fun lastAutoSelect() = when(lastAuto){
        "1" -> R.id.fan_mode_manual_btn
        "2" -> R.id.fan_mode_auto_btn
        else -> null
    }
    fun lastFanSelect() = when(lastFan){
        "0" -> R.id.off_btn
        "1" -> R.id.Low_btn
        "2" -> R.id.Medium_btn
        "3" -> R.id.height_btn
        else -> null
    }
}