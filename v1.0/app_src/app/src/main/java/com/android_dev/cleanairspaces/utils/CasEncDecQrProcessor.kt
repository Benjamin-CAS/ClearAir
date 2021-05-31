package com.android_dev.cleanairspaces.utils

import android.util.Base64
import android.util.Log
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.CONTROL_DEVICE_METHOD_KEY
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.DEVICE_INFO_METHOD_FOR_KEY
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.DEVICE_METHOD_FOR_KEY
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.INDOOR_LOCATION_DETAILS_METHOD_FOR_KEY
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.INDOOR_LOCATION_MONITORS_METHOD_FOR_KEY
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.LOCATION_INFO_METHOD_FOR_KEY
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.MONITOR_HISTORY_METHOD_FOR_KEY
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService.Companion.MONITOR_INFO_METHOD_FOR_KEY
import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails

object CasEncDecQrProcessor {

    private val TAG = CasEncDecQrProcessor::class.java.simpleName
    private const val QR_LOCATION_ERR = R.string.parsing_device_qr_failed
    private const val QR_UNKNOWN_ERR = R.string.scan_qr_code_unknown
    private const val SUCCESS = 200

    fun identifyQrCode(qrContent: String?): ParserResult {
        when {
            qrContent.isNullOrBlank() -> return ParserResult(codeRes = QR_UNKNOWN_ERR)
            qrContent.contains(DEFAULT_QR_LOCATION_ID) -> {
                val startIndexOfLocIdentifier = qrContent.indexOf(DEFAULT_QR_LOCATION_ID)
                val lastIndexOfLocIdentifier =
                    (startIndexOfLocIdentifier + DEFAULT_QR_LOCATION_ID.length) - 1
                if (startIndexOfLocIdentifier == -1 || lastIndexOfLocIdentifier == -1) return ParserResult(
                    codeRes = QR_LOCATION_ERR
                )

                val firstIndexOfLocId =
                    qrContent.indexOf(
                        DEFAULT_QR_LOCATION_ID_L_PAD,
                        startIndex = lastIndexOfLocIdentifier
                    ) + 1
                val lastIndexOfLocId =
                    qrContent.indexOf(
                        DEFAULT_QR_LOCATION_ID_R_PAD,
                        startIndex = lastIndexOfLocIdentifier
                    )

                if (firstIndexOfLocId == -1 || lastIndexOfLocIdentifier == -1) return ParserResult(
                    codeRes = QR_LOCATION_ERR
                )


                val companyId =
                    qrContent.substring(
                        startIndex = lastIndexOfLocIdentifier + 1,
                        endIndex = firstIndexOfLocId - 1
                    )

                val locationId =
                    qrContent.substring(startIndex = firstIndexOfLocId, endIndex = lastIndexOfLocId)

                if (locationId.isEmpty() || companyId.isEmpty()) return ParserResult(codeRes = QR_LOCATION_ERR)

                //we have a working location id
                val asciiViableLocId = getProperAsciiCheckedIds(locationId)
                val asciiViableCompanyId = getProperAsciiCheckedIds(companyId)
                val asciiViableLocIdAsInt = asciiViableLocId.toInt()
                val asciiViableCompIdAsInt = asciiViableCompanyId.toInt()
                if (asciiViableLocIdAsInt == 0 || asciiViableCompIdAsInt == 0)
                    return ParserResult(codeRes = QR_LOCATION_ERR)

                Log.d(
                    TAG,
                    "identifyQrCode($qrContent : String) locationId $locationId companyId $companyId " +
                            "locAsInt $asciiViableLocIdAsInt compAsInt $asciiViableCompIdAsInt"
                )
                return ParserResult(
                    codeRes = SUCCESS,
                    locId = asciiViableLocIdAsInt,
                    compId = asciiViableCompIdAsInt,
                    extraData = "company ID $companyId @location ID $locationId"
                )
            }
            qrContent.length == QR_MONITOR_ID_PADDED_LENGTH || qrContent.length == QR_MONITOR_ID_TRUE_LENGTH -> {
                // This is a monitorID, let's get the location and try to add it
                val properMonitorId =
                    if (qrContent.length == QR_MONITOR_ID_PADDED_LENGTH) {
                        qrContent.substring(QR_MONITOR_ID_PAD_LENGTH until QR_MONITOR_ID_PADDED_LENGTH - QR_MONITOR_ID_PAD_LENGTH)
                    } else qrContent

                return ParserResult(
                    codeRes = SUCCESS,
                    monitorId = properMonitorId,
                    extraData = properMonitorId
                )
            }
            else -> return ParserResult(codeRes = QR_UNKNOWN_ERR)
        }
    }

    private fun getProperAsciiCheckedIds(strId: String): String {
        var asciiViableId = ""
        for (char in strId) {
            val charAsInt = char.toInt()
            if (charAsInt in 65..73) {

                val c = (charAsInt - 17).toChar()
                asciiViableId += c
            }

        }
        return asciiViableId
    }


    private fun calculateEncKey(payload: String, key: String): String {
        var trueKey = ""
        val payLoadLength = payload.length
        while (trueKey.length < payLoadLength) {
            trueKey += key
        }
        return trueKey.substring(0, payload.length)
    }

    private fun doCASEncryptOrDecrypt(payload: String, key: String): String {
        val trueKey = calculateEncKey(payload, key)
        var encodedStr = ""
        for ((index, letter) in payload.withIndex()) {
            val keyForCurrentPos = trueKey[index]
            val code = (keyForCurrentPos.toInt()).xor(letter.toInt())
            encodedStr += if (code < 32 || code > 126) {
                letter
            } else code.toChar()
        }
        return encodedStr
    }

    private fun toBase64Encoding(encryptedPayload: String): String {
        return Base64.encodeToString(encryptedPayload.encodeToByteArray(), Base64.NO_PADDING).trim()
    }

    private fun fromBase64Encoding(base64EncodeStr: String): String {
        return Base64.decode(base64EncodeStr.encodeToByteArray(), Base64.NO_PADDING)
            .decodeToString()
            .trim()
    }


    fun getEncryptedEncodedPayloadForLocationHistory(
        compId: String,
        locId: String,
        userName: String,
        userPassword: String,
        timeStamp: String
    ): String {
        val key = "${DEVICE_INFO_METHOD_FOR_KEY}$timeStamp"
        val pl =
            "{\"$COMP_ID_KEY\":\"$compId\",\"$LOC_ID_KEY\":\"$locId\",\"$USER_KEY\":\"$userName\",\"$PASSWORD_KEY\":\"$userPassword\",\"$HISTORY_KEY\":\"1\",\"$HISTORY_WEEK_KEY\":\"1\",\"$HISTORY_DAY_KEY\":\"1\",\"$PM25_TYPE_PARAM_KEY\":\"$PM2_5_STD_PARAM\"}"
        val casEncrypted = doCASEncryptOrDecrypt(payload = pl, key = key)
        val encoded = toBase64Encoding(casEncrypted)
        Log.d(
            TAG,
            "getEncryptedEncodedPayloadForLocationHistory($compId: compId, $locId: locId, $userName: username, $userPassword: upass, $timeStamp: time) true key $key pl $pl encrypted $casEncrypted  encoded $encoded"
        )
        return encoded
    }

    fun decodeApiResponse(payload: String): String {
        return fromBase64Encoding(payload)
    }

    fun getEncryptedEncodedPayloadForScannedDeviceWithCompLoc(
        locId: Int,
        compId: Int,
        timeStamp: String
    ): String {
        val key = "${LOCATION_INFO_METHOD_FOR_KEY}$timeStamp"
        val payload = "{\"$COMP_ID_KEY\":$compId, \"$LOC_ID_KEY\":$locId}"
        val casEncrypted = doCASEncryptOrDecrypt(payload = payload, key = key)
        Log.d(
            TAG,
            "getEncryptedEncodedPayloadForScannedDeviceWithCompLoc($locId : L, $compId : C, $timeStamp : t) true key $key payload $payload encrypted $casEncrypted"
        )
        return toBase64Encoding(casEncrypted)
    }

    fun getEncryptedEncodedPayloadForScannedDeviceWithMonitorId(
        monitorId: String,
        timeStamp: String
    ): String {
        val key = "${MONITOR_INFO_METHOD_FOR_KEY}$timeStamp"
        val casEncrypted = doCASEncryptOrDecrypt(payload = monitorId, key = key)
        Log.d(
            TAG,
            "getEncryptedEncodedPayloadForScannedDeviceWithMonitorId(monitorId: $monitorId, $timeStamp : time) true key $key  encrypted $casEncrypted"
        )
        return toBase64Encoding(casEncrypted)
    }

    fun getEncryptedEncodedPayloadForIndoorLocation(timeStamp: String): String {
        val key = "${LOCATION_INFO_METHOD_FOR_KEY}$timeStamp"
        val payload = "ltime$timeStamp"
        val casEncrypted = doCASEncryptOrDecrypt(payload = payload, key = key)
        Log.d(
            TAG,
            "getEncryptedEncodedPayloadForIndoorLocation(timeStamp: $timeStamp) key $key payload $payload encrypted $casEncrypted"
        )
        return toBase64Encoding(casEncrypted)
    }

    fun getEncryptedEncodedPayloadForLocationDetails(
        compId: String,
        locId: String,
        userName: String,
        userPassword: String,
        timeStamp: String,
        isIndoorLoc: Boolean
    ): String {
        val key = "${DEVICE_INFO_METHOD_FOR_KEY}$timeStamp"
        val payload =
            "{\"$COMP_ID_KEY\":\"$compId\",\"$LOC_ID_KEY\":\"$locId\",\"$USER_KEY\":\"$userName\",\"$PASSWORD_KEY\":\"$userPassword\",\"$HISTORY_KEY\":\"0\",\"$HISTORY_WEEK_KEY\":\"0\",\"$HISTORY_DAY_KEY\":\"0\",\"$PM25_TYPE_PARAM_KEY\":\"0\"}".replace(
                " ",
                ""
            )
        val casEncrypted = doCASEncryptOrDecrypt(payload = payload, key = key)
        val encoded = toBase64Encoding(casEncrypted)
        Log.d(
            TAG,
            "getEncryptedEncodedPayloadForLocationDetails($compId: compId, $locId: locId, $userName: username, $userPassword: upass, $timeStamp: time, isIndoorLoc $isIndoorLoc) true key $key encrypted $casEncrypted  encoded $encoded"
        )
        return encoded
    }

    fun getEncryptedEncodedPayloadForOutdoorLocation(timeStamp: String): String {
        val payload = "{\"d\":\"$1\"}"
        val casEncrypted = doCASEncryptOrDecrypt(payload = payload, key = timeStamp)
        val encoded = toBase64Encoding(casEncrypted)
        Log.d(
            TAG,
            "getEncryptedEncodedPayloadForOutdoorLocation() true key $timeStamp encrypted $casEncrypted  encoded $encoded"
        )
        return encoded
    }

    fun getEncryptedEncodedPayloadForIndoorLocationOverviewDetails(
        timeStamp: String,
        companyId: String,
        userName: String,
        userPass: String
    ): String {
        val key = "${INDOOR_LOCATION_DETAILS_METHOD_FOR_KEY}$timeStamp"
        val payload =
            "{\"$COMP_ID_KEY\":\"$companyId\",\"$USER_KEY\":\"$userName\",\"$PASSWORD_KEY\":\"$userPass\"}"
        val casEncrypted = doCASEncryptOrDecrypt(payload = payload, key = key)
        return toBase64Encoding(casEncrypted)
    }

    fun getEncryptedEncodedPayloadForIndoorLocationMonitors(
        timeStamp: String,
        companyId: String,
        locId: String,
        userName: String,
        userPass: String
    ): String {
        val key = "${INDOOR_LOCATION_MONITORS_METHOD_FOR_KEY}$timeStamp"
        val payload =
            "{\"$COMP_ID_KEY\":\"$companyId\",\"$LOC_ID_KEY\":\"$locId\",\"$USER_KEY\":\"$userName\",\"$PASSWORD_KEY\":\"$userPass\",\"p\":\"$PM2_5_STD_PARAM\" }"
        val casEncrypted = doCASEncryptOrDecrypt(payload = payload, key = key)
        return toBase64Encoding(casEncrypted)
    }

    fun getEncryptedEncodedPayloadForMonitorHistory(
        compId: String,
        locId: String,
        monitorId: String,
        userName: String,
        userPassword: String,
        timeStamp: String
    ): String {
        val key = "${MONITOR_HISTORY_METHOD_FOR_KEY}$timeStamp"
        val pl =
            "{\"$COMP_ID_KEY\":\"$compId\",\"$LOC_ID_KEY\":\"$locId\",\"$MON_ID_KEY\":\"$monitorId\",\"$USER_KEY\":\"$userName\",\"$PASSWORD_KEY\":\"$userPassword\",\"$HISTORY_KEY\":\"1\",\"$HISTORY_WEEK_KEY\":\"1\",\"$HISTORY_DAY_KEY\":\"1\"}"
        val casEncrypted = doCASEncryptOrDecrypt(payload = pl, key = key)
        val encoded = toBase64Encoding(casEncrypted)
        Log.d(
            TAG,
            "getEncryptedEncodedPayloadForMonitorHistory(encoded $encoded"
        )
        return encoded
    }

    fun getEncryptedEncodedPayloadForDevices(
        timeStamp: String,
        companyId: String,
        locId: String,
        userName: String,
        userPass: String
    ): String {
        val key = "${DEVICE_METHOD_FOR_KEY}$timeStamp"
        val pl =
            "{\"$COMP_ID_KEY\":\"$companyId\",\"$LOC_ID_KEY\":\"$locId\",\"$USER_KEY\":\"$userName\",\"$PASSWORD_KEY\":\"$userPass\"}"
        val casEncrypted = doCASEncryptOrDecrypt(payload = pl, key = key)
        val encoded = toBase64Encoding(casEncrypted)
        Log.d(
            TAG,
            "getEncryptedEncodedPayloadForDevices(encoded $encoded"
        )
        return encoded
    }

    fun getEncryptedEncodedPayloadForControllingDevices(
        device: DevicesDetails,
        timeStamp: String
    ): String {
        val key = "${CONTROL_DEVICE_METHOD_KEY}$timeStamp"
        val faOrDf =
            if (DevicesTypes.getDeviceInfoByType(device.device_type)?.hasDuctFit == true) device.df else device.fa
        val pl =
            "{\"$COMP_ID_KEY\":\"${device.compId}\",\"$LOC_ID_KEY\":\"${device.locId}\",\"$USER_KEY\":\"${device.lastRecUname}\",\"$PASSWORD_KEY\":\"${device.lastRecPwd}\",\"mac\":\"${device.mac}\",\"device_type\":\"${device.device_type}\",\"fan\":\"${device.fan_speed}\",\"df\":\"${faOrDf}\",\"mode\":\"${device.mode}\"}"
        val casEncrypted = doCASEncryptOrDecrypt(payload = pl, key = key)
        val encoded = toBase64Encoding(casEncrypted)
        Log.d(
            TAG,
            "getEncryptedEncodedPayloadForControllingDevices(encoded $encoded"
        )
        return encoded
    }
}

data class ParserResult(
    val codeRes: Int,
    val locId: Int? = null,
    val compId: Int? = null,
    val monitorId: String? = null,
    val extraData: String = ""
)

const val PM2_5_STD_PARAM = "0"