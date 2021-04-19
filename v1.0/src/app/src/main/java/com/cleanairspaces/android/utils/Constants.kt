package com.cleanairspaces.android.utils


const val BASE_URL = "https://monitor.cleanairspaces.com/"
const val DEFAULT_LOCATION_EN_NAME = "Unspecified Location"
const val DATABASE_NAME = "clean_air_spaces_db"
const val OUTDOOR_LOCATIONS_REFRESH_RATE_MIN = 30L // 30 minutes
const val MY_LOCATIONS_REFRESH_RATE_MIN = 15L // 15 minutes
const val SCANNING_QR_TIMEOUT_MILLS = 60000L //1 minute
const val HISTORY_EXPIRE_TIME_MILLS = 60000L //TODO 900000L 15 minutes
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
const val API_APP_ID = 1
const val L_TIME_KEY = "ltime"
const val PAYLOAD_KEY = "pl"
const val UPDATE_USER_LOCATION_INTERVAL = 300000L //5 minutes
const val HEAT_MAP_CIRCLE_RADIUS = 50

//worker tags
const val LOCATIONS_REFRESH_WORKER = "locations_refresher"
