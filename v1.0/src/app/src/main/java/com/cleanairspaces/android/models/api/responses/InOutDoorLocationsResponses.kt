package com.cleanairspaces.android.models.api.responses

import android.service.voice.AlwaysOnHotwordDetector
import com.google.gson.annotations.Expose


data class OutDoorLocationsOtherResponse(
    @Expose
    val data: List<OutDoorLocationsOther>,
)

data class OutDoorLocationsOther(
    @Expose
    val location_id: String,
    @Expose
    val monitor_id: String,
    @Expose
    val name_en: String,
    @Expose
    val lon: String,
    @Expose
    val lat: String,
    @Expose
    val reading: String,
    @Expose
    val date_reading: String,
)


/*********** AMERICA ************/
data class OutDoorLocationAmerica(
    @Expose
    val pm2p5: String,
    @Expose
    val sta_lon: String,
    @Expose
    val sta_lat: String
)

/*********** TAIWAN ************/
data class OutDoorLocationTaiwan(
    @Expose
    val pm2p5: String,
    @Expose
    val lon: String,
    @Expose
    val lat: String
)

/************* INDOOR **********/
data class IndoorLocationsResponse(
    @Expose
    val status :Boolean,
    @Expose
    val code : Int,
    @Expose
    val data: List<IndoorLocations>
    )

data class IndoorLocations(
    @Expose
 val company_id : String,
    @Expose
 val name_en : String,
    @Expose
 val secure :  Number,
    @Expose
 val active : Number,
    @Expose
 val outdoor : Number,

)
