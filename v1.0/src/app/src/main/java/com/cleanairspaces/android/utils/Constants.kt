package com.cleanairspaces.android.utils


const val BASE_URL = "https://monitor.cleanairspaces.com/"
const val DEFAULT_LOCATION_EN_NAME = "Unspecified Location"
const val DATABASE_NAME = "clean_air_spaces_db"
const val OUTDOOR_LOCATIONS_REFRESH_RATE_MILLS = 1800000L //30 minutes
const val SCANNING_QR_TIMEOUT_MILLS = 60000L //1 minute
const val DEFAULT_QR_LOCATION_ID = "LOCID"
const val DEFAULT_QR_LOCATION_ID_L_PAD = "X"
const val DEFAULT_QR_LOCATION_ID_R_PAD = "Y"
const val QR_MONITOR_ID_PAD_LENGTH = 5
const val QR_MONITOR_ID_PADDED_LENGTH = 22
const val QR_MONITOR_ID_TRUE_LENGTH = 12
const val COMP_ID_KEY = "c"
const val LOC_ID_KEY = "l"
const val USER_KEY = "user"
const val HISTORY_KEY = "h"
const val HISTORY_WEEK_KEY = "w"
const val HISTORY_DAY_KEY = "d"
const val PM25_TYPE_PARAM_KEY = "p"
const val PASSWORD_KEY = "password"
const val NONCE = "aa"
const val L_TIME_KEY = "ltime"
const val PAYLOAD_KEY = "pl"
const val UPDATE_USER_LOCATION_INTERVAL = 300000L //5 minutes
const val HEAT_MAP_CIRCLE_RADIUS = 50




//todo code to remove
/*
    //prepare bitmaps
    val aQIGoodBitmap = R.drawable.good_circle
    val aQIModerateBitmap = R.drawable.moderate_circle
    val aQIGUnhealthyBitmap = R.drawable.g_unhealthy_circle
    val aQIUnhealthyBitmap = R.drawable.unhealthy_circle
    val aQIVUnhealthyBitmap = R.drawable.v_unhealthy_circle
    val aQIHazardousBitmap = R.drawable.hazardous_circle
    val aQIBeyondBitmap = R.drawable.beyond_circle
    val aQICNExcellentBitmap = R.drawable.excellent
 */
/*private fun setupMarkers(locations: List<OutDoorLocations>) {
    for (location in locations) {
        val mDrawable = getIconForMarker(location)
        val mIcon = if (mDrawable == null) null else
            BitmapDescriptorFactory.fromBitmap(
                BitmapFactory
                    .decodeResource(resources, mDrawable)
            )

        val markerOptions = MarkerOptions()
        markerOptions.apply {
            position(location.getAMapLocationLatLng())
            draggable(false)
            anchor(0.5f, 0.5f)
            mIcon?.let {
                icon(it)
            }
            aMap?.addMarker(markerOptions)
        }
    }
}

private fun getIconForMarker(location: OutDoorLocations): Int? {
    val pm25 = if (location.pm2p5 != "") location.pm2p5 else location.reading
    return when (AQI.getAQIStatusColorFromPM25(pm25.toDouble())) {
        UIColor.AQIGoodColor -> aQIGoodBitmap
        UIColor.AQIModerateColor -> aQIModerateBitmap
        UIColor.AQIGUnhealthyColor -> aQIGUnhealthyBitmap
        UIColor.AQIUnhealthyColor -> aQIUnhealthyBitmap
        UIColor.AQIVUnhealthyColor -> aQIVUnhealthyBitmap
        UIColor.AQIHazardousColor -> aQIHazardousBitmap
        UIColor.AQIBeyondColor -> aQIBeyondBitmap
        UIColor.AQICNExcellentColor -> aQICNExcellentBitmap
        else -> null
    }
}*/