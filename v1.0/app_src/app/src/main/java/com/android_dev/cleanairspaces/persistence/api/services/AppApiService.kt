package com.android_dev.cleanairspaces.persistence.api.services

import com.android_dev.cleanairspaces.BuildConfig


interface AppApiService {
    companion object {
        const val APP_USER = "pablo"
        const val APP_USER_PWD = "cleanair"
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
        const val MONITOR_HISTORY_METHOD_FOR_KEY = "MonDataGetCAS"
        const val MONITOR_HISTORY_METHOD = "MonDataGet"
        const val DEVICE_METHOD_FOR_KEY = "GetDevicesByLocIdCAS"
        const val DEVICE_METHOD = "GetDevicesByLocId"
        const val CONTROL_DEVICE_METHOD = "SetDevice"
        const val CONTROL_DEVICE_METHOD_KEY = "SetDeviceCAS"
    }
}