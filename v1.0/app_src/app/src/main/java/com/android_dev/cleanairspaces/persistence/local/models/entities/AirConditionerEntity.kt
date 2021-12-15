package com.android_dev.cleanairspaces.persistence.local.models.entities

import androidx.annotation.IdRes
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
        "1" -> AirConditionerItemData(R.id.independent_btn,"1","Independent")
        "2" -> AirConditionerItemData(R.id.inside_zone_btn,"2","Inside Zone")
        else -> null
    }
    fun lastModeSelect() = when(mode){
        "0" -> AirConditionerItemData(R.id.fan_btn,"0","Fan")
        "1" -> AirConditionerItemData(R.id.cooling_btn,"1","Cooling")
        "2" -> AirConditionerItemData(R.id.heating_btn,"2","Heating")
        "3" -> AirConditionerItemData(R.id.auto_btn,"3","Auto")
        else -> null
    }
    fun lastAutoSelect() = when(auto){
        "1" -> AirConditionerItemData(R.id.fan_mode_manual_btn,"1","Manual")
        "2" -> AirConditionerItemData(R.id.fan_mode_auto_btn,"2","Auto")
        else -> null
    }
    fun lastFanSelect() = when(fanSpeed){
        "0" -> AirConditionerItemData(R.id.off_btn,"0","Off")
        "1" -> AirConditionerItemData(R.id.Low_btn,"1","Low")
        "2" -> AirConditionerItemData(R.id.Medium_btn,"2","Medium")
        "3" -> AirConditionerItemData(R.id.height_btn,"3","Height")
        else -> null
    }
}

data class AirConditionerItemData(
    @IdRes
    val btnId:Int,
    val num:String,
    val text:String
)