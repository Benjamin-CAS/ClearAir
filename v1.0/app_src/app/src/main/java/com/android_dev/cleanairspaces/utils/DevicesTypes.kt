package com.android_dev.cleanairspaces.utils

import com.android_dev.cleanairspaces.R

object DevicesTypes {
    fun getDeviceInfoByType(deviceTypeStr: String): DeviceInfo? {
        return when (deviceTypeStr.toInt()) {
            1 -> {
                DeviceInfo(
                    deviceTitleRes = R.string.device_type_one,
                    deviceLogoName = "cmd.png",
                    deviceTrueName = "CMD 900 DF",
                    hasFanCalibrations = true,
                    hasFanBasic = false,
                    hasDuctFit = true
                )
            }

            3 -> {
                DeviceInfo(
                    deviceTitleRes = R.string.device_type_three,
                    deviceLogoName = "cmd.png",
                    deviceTrueName = "EP550",
                    hasFanCalibrations = false,
                    hasFanBasic = true,
                    hasDuctFit = false
                )
            }
            4 -> {
                DeviceInfo(
                    deviceTitleRes = R.string.device_type_four,
                    deviceLogoName = "cmd.png",
                    deviceTrueName = "CMD 900",
                            hasFanCalibrations = true,
                    hasFanBasic = false,
                    hasDuctFit = true
                )
            }

            5 -> {
                DeviceInfo(
                    deviceTitleRes = R.string.device_type_five,
                    deviceLogoName = "KleanSense.png",
                    deviceTrueName = "KS",
                    hasFanCalibrations = false,
                    hasFanBasic = true,
                    hasDuctFit = false
                )
            }

            6 -> {
                DeviceInfo(
                    deviceTitleRes = R.string.device_type_six,
                    deviceLogoName = "cmd.png",
                    deviceTrueName = "FA",
                            hasFanCalibrations = false,
                    hasFanBasic = true,
                    hasDuctFit = false
                )
            }

            8 -> {
                DeviceInfo(
                    deviceTitleRes = R.string.device_type_eight,
                    deviceLogoName = "cmd.png",
                    deviceTrueName = "CMD900 FAD",
                    hasFanCalibrations = true,
                    hasFanBasic = false,
                    hasDuctFit = false,
                    hasFreshAir = true
                )
            }

            9 -> {
                DeviceInfo(
                    deviceTitleRes = R.string.device_type_nine,
                    deviceLogoName = "ductfit.png",
                    deviceTrueName = "ductFit",
                    hasFanCalibrations = false,
                    hasFanBasic = true,
                    hasDuctFit = true
                )
            }

            10 -> {
                DeviceInfo(
                    deviceTitleRes = R.string.device_type_ten,
                    deviceLogoName = "ductfit.png",
                    deviceTrueName = "FA3",
                    hasFanCalibrations = true,
                    hasFanBasic = false,
                    hasDuctFit = false
                )
            }

            11 -> {
                DeviceInfo(
                    deviceTitleRes = R.string.device_type_eleven,
                    deviceLogoName = "dfCloud.png",
                    deviceTrueName = "DFC",
                    hasFanCalibrations = true,
                    hasFanBasic = false,
                    hasDuctFit = true
                )
            }

            12 -> {
                DeviceInfo(
                    deviceTitleRes = R.string.device_type_twelve,
                    deviceLogoName = "dfWall.png",
                    deviceTrueName = "DFW",
                    hasFanCalibrations = true,
                    hasFanBasic = false,
                    hasDuctFit = true
                )
            }

            13 -> {
                DeviceInfo(
                    deviceTitleRes = R.string.device_type_thirteen,
                    deviceLogoName = "",
                    deviceTrueName = "DFM",
                    hasExtendedFanCalibrations = true,
                    hasFanCalibrations = false,
                    hasFanBasic = false,
                    hasDuctFit = false //todo has but NOT shown
                )
            }
            else -> null //todo ? other
        }
    }

    data class DeviceInfo(
        val deviceTitleRes: Int,
        val deviceLogoName: String,
        val deviceTrueName : String,
        val hasMode : Boolean = true,
        val hasExtendedFanCalibrations : Boolean = false, //OFF TURBO-SLOW LOW MED HIGH TURBO
        val hasFanCalibrations : Boolean, //off low med & high
        val hasFanBasic : Boolean, //on or off only
        val hasDuctFit : Boolean,
        val hasFreshAir : Boolean  = false
    )
}