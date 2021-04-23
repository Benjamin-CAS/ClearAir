package com.android_dev.cleanairspaces.utils

import com.android_dev.cleanairspaces.R

object MonitorTypes {
    fun getDeviceInfoByType(deviceTypeStr: String): DeviceInfo? {
        return when (deviceTypeStr.toInt()) {
            1 -> {
                DeviceInfo(
                        deviceTitleRes = R.string.device_type_one,
                        deviceLogoName = "cmd.png"
                )
            }

            3 -> {
                DeviceInfo(
                        deviceTitleRes = R.string.device_type_three,
                        deviceLogoName = "cmd.png"
                )
            }
            4 -> {
                DeviceInfo(
                        deviceTitleRes = R.string.device_type_four,
                        deviceLogoName = "cmd.png"
                )
            }

            5 -> {
                DeviceInfo(
                        deviceTitleRes = R.string.device_type_five,
                        deviceLogoName = "KleanSense.png"
                )
            }

            6 -> {
                DeviceInfo(
                        deviceTitleRes = R.string.device_type_six,
                        deviceLogoName = "cmd.png"
                )
            }

            8 -> {
                DeviceInfo(
                        deviceTitleRes = R.string.device_type_eight,
                        deviceLogoName = "cmd.png"
                )
            }

            9 -> {
                DeviceInfo(
                        deviceTitleRes = R.string.device_type_nine,
                        deviceLogoName = "ductfit.png"
                )
            }

            10 -> {
                DeviceInfo(
                        deviceTitleRes = R.string.device_type_ten,
                        deviceLogoName = "ductfit.png"
                )
            }

            11 -> {
                DeviceInfo(
                        deviceTitleRes = R.string.device_type_eleven,
                        deviceLogoName = "dfCloud.png"
                )
            }

            12 -> {
                DeviceInfo(
                        deviceTitleRes = R.string.device_type_twelve,
                        deviceLogoName = "dfWall.png"
                )
            }
            else -> null //todo ? other
        }
    }

    data class DeviceInfo(
            val deviceTitleRes: Int,
            val deviceLogoName: String
    )
}