package com.android_dev.cleanairspaces.persistence.api.responses

import com.google.gson.annotations.Expose

data class OutDoorDetailsLocationResponse(
        @Expose
        val data: List<OutDoorDetailedLocationData>,
)

data class OutDoorDetailedLocationData(
        @Expose
        val company_id: String,
        val logo: String?,
        @Expose
        val location_id: String,
        @Expose
        val location_name: String,
        @Expose
        val city: String,
        @Expose
        val country: String,
        @Expose
        val lon: String,
        @Expose
        val lat: String
)

data class OutDoorLocationResponse(
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