package com.android_dev.cleanairspaces.persistence.api.responses

import com.google.gson.annotations.Expose


data class GetAirConditionerRes(
    @Expose
    val code: Int,
    @Expose
    val data: List<Data>,
    @Expose
    val status: Boolean
)
data class Data(
    @Expose
    val auto: String,
    @Expose
    val current: String,
    @Expose
    val dev_name: String,
    @Expose
    val device_type: String,
    @Expose
    val fan_speed: String,
    @Expose
    val id: Int,
    @Expose
    val iforce: String,
    @Expose
    val last_auto: String,
    @Expose
    val last_fan: String,
    @Expose
    val last_mode: String,
    @Expose
    val last_time: String,
    @Expose
    val location_id: String,
    @Expose
    val mac: String,
    @Expose
    val mode: String,
    @Expose
    val name_en: String,
    @Expose
    val status: Int,
    @Expose
    val t_cal: String,
    @Expose
    val target: String,
    @Expose
    val version: String,
    @Expose
    val zone_id: String,
    @Expose
    val zone_mode: String
)