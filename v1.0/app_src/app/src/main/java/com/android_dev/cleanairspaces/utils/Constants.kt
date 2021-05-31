package com.android_dev.cleanairspaces.utils

// settings
const val SETTINGS_FILE_NAME = "settings"
const val MAP_TO_USE_KEY = "map_choice"
const val AQI_INDEX_TO_USE_KEY = "api_index_choice"
const val MAP_LANG_TO_USE_KEY = "language_in_map_choice"
const val HAS_REQUESTED_LOC_PERMISSION = "req_loc_perm_without_prompt"

//NOTE these correspond with strings array
const val DEFAULT_AQI_INDEX_PM25 = "PM 2.5"
const val AQI_INDEX_AQI_US = "AQI U.S"


//API
const val BASE_URL = "https://monitor.cleanairspaces.com/"
const val NONCE = "aa"
const val API_APP_ID = 1

//persistence
const val DATABASE_NAME = "clean_air_spaces_db"


//worker threads
const val DATA_REFRESHER_WORKER_NAME = "data_refresh_1193"
const val DATA_REFRESH_INTERVAL_MIN = 15L


//MAPS
const val USER_LOCATION_UPDATE_INTERVAL_MILLS = 120000L //2 minutes
const val USER_LOCATION_UPDATE_ON_DISTANCE = 50f //50 meters
const val MY_LOCATION_ZOOM_LEVEL = 8f


//misc
const val SCANNING_QR_TIMEOUT_MILLS = 18000L //3 minutes

//DATA
const val HISTORY_EXPIRE_TIME_MILLS = 900000L //15 minutes
const val DEFAULT_QR_LOCATION_ID = "LOCID"
const val DEFAULT_QR_LOCATION_ID_L_PAD = "X"
const val DEFAULT_QR_LOCATION_ID_R_PAD = "Y"
const val QR_MONITOR_ID_PAD_LENGTH = 5
const val QR_MONITOR_ID_PADDED_LENGTH = 22
const val QR_MONITOR_ID_TRUE_LENGTH = 12
const val API_LOCAL_DATA_BINDER_KEY = "tag"
const val COMP_ID_KEY = "c"
const val LOC_ID_KEY = "l"
const val MON_ID_KEY = "m"
const val USER_KEY = "user"
const val HISTORY_KEY = "h"
const val HISTORY_WEEK_KEY = "w"
const val HISTORY_DAY_KEY = "d"
const val PM25_TYPE_PARAM_KEY = "p"
const val PASSWORD_KEY = "password"
const val L_TIME_KEY = "ltime"
const val PAYLOAD_KEY = "pl"
const val MONITOR_ID_KEY = "monitor_id"
const val LAT_LON_DELIMITER = "_"


//device refresh
const val REFRESHED_DEVICE_DELAY = 8000L




