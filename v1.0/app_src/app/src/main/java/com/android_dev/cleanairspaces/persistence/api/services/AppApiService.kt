package com.android_dev.cleanairspaces.persistence.api.services

import com.android_dev.cleanairspaces.BuildConfig

interface AppApiService {
    companion object {
        const val APP_USER = BuildConfig.APP_USER
        const val APP_USER_PWD = BuildConfig.APP_USER_PWD

        const val LOCATION_INFO_METHOD = "GetDevInfoById"
        const val LOCATION_INFO_METHOD_FOR_KEY = "GetDevInfoByIdCAS"
        const val MONITOR_INFO_METHOD_FOR_KEY = "GetDevInfoCAS"
        const val MONITOR_INFO_METHOD = "GetDevInfo"
        const val DEVICE_INFO_METHOD_FOR_KEY = "LocDataGetCAS"
        const val DEVICE_INFO_METHOD = "LocDataGet"
        const val INDOOR_LOCATION_DETAILS_METHOD = "LocGet"
        const val INDOOR_LOCATION_DETAILS_METHOD_FOR_KEY = "LocGetCAS"
        const val INDOOR_LOCATION_MONITORS_METHOD = "LocAll"
        const val INDOOR_LOCATION_MONITORS_METHOD_FOR_KEY = "LocAllCAS"

    }
}