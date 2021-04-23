package com.cleanairspaces.android.utils


import android.util.Base64
import android.util.Base64.decode
import android.util.Base64.encodeToString
import com.cleanairspaces.android.R
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.DEVICE_INFO_METHOD_FOR_KEY
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.INDOOR_LOCATION_DETAILS_METHOD_FOR_KEY
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.LOCATION_INFO_METHOD_FOR_KEY
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.MONITOR_INFO_METHOD_FOR_KEY


/*
**
//example monitor qrContent == http://monitor.cleanairspaces.com/downloadApp?LOCIDBHBXGEGYCEIZ
//or device qrContent ==  msg : qrContent NATDF483FDA0DBCB000000
 */
object QrCodeProcessor {

    private val TAG = QrCodeProcessor::class.java.simpleName
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

                MyLogger.logThis(
                    TAG, "identifyQrCode($qrContent : String)",
                    "locationId $locationId companyId $companyId " +
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
        return encodeToString(encryptedPayload.encodeToByteArray(), Base64.NO_PADDING).trim()
    }

    private fun fromBase64Encoding(base64EncodeStr: String): String {
        return decode(base64EncodeStr.encodeToByteArray(), Base64.NO_PADDING).decodeToString()
            .trim()
    }


    fun getEncryptedEncodedPayloadForLocation(locId: Int, compId: Int, timeStamp: String): String {
        val key = getProperPayloadKeyForCompLocation(timeStamp)
        MyLogger.logThis(
            TAG,
            "getEncryptedEncodedPayload($locId : L, $compId : C, $timeStamp : t)",
            "true key $key"
        )
        val payload = getProperPayload(locId, compId)
        MyLogger.logThis(TAG, "getEncryptedEncodedPayload()", "payload $payload")
        val casEncrypted = doCASEncryptOrDecrypt(payload = payload, key = key)
        MyLogger.logThis(TAG, "getEncryptedEncodedPayload()", "encrypted $casEncrypted")
        return toBase64Encoding(casEncrypted)
    }

    fun getEncryptedEncodedPayloadForIndoorLocation(timeStamp: String): String {
        val key = getProperPayloadKeyForCompLocation(timeStamp)
        val payload = "ltime$timeStamp"
        val casEncrypted = doCASEncryptOrDecrypt(payload = payload, key = key)
        return toBase64Encoding(casEncrypted)
    }

    fun getEncryptedEncodedPayloadForMonitor(monitorId: String, timeStamp: String): String {
        val key = getProperPayloadKeyForMonitor(timeStamp)
        MyLogger.logThis(
            TAG,
            "getEncryptedEncodedPayloadForMonitor(monitorId: $monitorId, $timeStamp : time)",
            "true key $key"
        )
        val casEncrypted = doCASEncryptOrDecrypt(payload = monitorId, key = key)
        MyLogger.logThis(TAG, "getEncryptedEncodedPayloadForMonitor()", "encrypted $casEncrypted")
        return toBase64Encoding(casEncrypted)
    }

    fun getEncryptedEncodedPayloadForDeviceDetails(
            compId: String,
            locId: String,
            userName: String,
            userPassword: String,
            timeStamp: String,
            showHistory: Boolean = false,
            isIndoorLoc : Boolean = false
    ): String {
        val key = getProperPayloadKeyForDeviceDetails(isIndoorLoc = isIndoorLoc, timeStamp=timeStamp)
        val pl = getProperPayloadForDeviceDetails(
            compId = compId,
            locId = locId,
            userName = userName,
            userPassword = userPassword,
                showHistory = showHistory
        )
        val casEncrypted = doCASEncryptOrDecrypt(payload = pl, key = key)
        val encoded = toBase64Encoding(casEncrypted)
        MyLogger.logThis(
                TAG,
                "getEncryptedEncodedPayloadForDeviceDetails($compId: compId, $locId: locId, $userName: username, $userPassword: upass, $timeStamp: time, isIndoorLoc $isIndoorLoc)",
                "true key $key @pl $pl encrypted $casEncrypted  encoded $encoded"
        )
        return encoded
    }


    /***************** PAYLOADS *******************/
    private fun getProperPayload(locId: Int, compId: Int): String {
        return "{\"$COMP_ID_KEY\":$compId, \"$LOC_ID_KEY\":$locId}"
    }

    private fun getProperPayloadForDeviceDetails(
        compId: String,
        locId: String,
        userName: String,
        userPassword: String,
        showHistory: Boolean = false,
        pmStandard: String = PM2_5_STD_PARAM
    ): String {
        return if (!showHistory)
            "{\"$COMP_ID_KEY\":\"$compId\",\"$LOC_ID_KEY\":\"$locId\",\"$USER_KEY\":\"$userName\",\"$PASSWORD_KEY\":\"$userPassword\",\"$HISTORY_KEY\":\"0\",\"$HISTORY_WEEK_KEY\":\"0\",\"$HISTORY_DAY_KEY\":\"0\",\"$PM25_TYPE_PARAM_KEY\":\"$pmStandard\"}"
        else
            "{\"$COMP_ID_KEY\":\"$compId\",\"$LOC_ID_KEY\":\"$locId\",\"$USER_KEY\":\"$userName\",\"$PASSWORD_KEY\":\"$userPassword\",\"$HISTORY_KEY\":\"1\",\"$HISTORY_WEEK_KEY\":\"1\",\"$HISTORY_DAY_KEY\":\"1\",\"$PM25_TYPE_PARAM_KEY\":\"$pmStandard\"}"

    }


    /***************** KEYS *******************/
    private fun getProperPayloadKeyForCompLocation(timeStamp: String): String {
        return "${LOCATION_INFO_METHOD_FOR_KEY}$timeStamp"
    }

    private fun getProperPayloadKeyForMonitor(timeStamp: String): String {
        return "${MONITOR_INFO_METHOD_FOR_KEY}$timeStamp"
    }

    private fun getProperPayloadKeyForDeviceDetails(isIndoorLoc: Boolean, timeStamp: String): String {
        return if (isIndoorLoc)
            "${INDOOR_LOCATION_DETAILS_METHOD_FOR_KEY}$timeStamp"
            else "${DEVICE_INFO_METHOD_FOR_KEY}$timeStamp"
    }

    /********************** UN ENCRYPTIONS **************/
    fun getUnEncryptedPayload(encPayload: String, lTime: String, forCompLocation: Boolean): String {
        return fromBase64Encoding(encPayload)
    }

    fun getUnEncryptedPayloadForLocationDetails(payload: String, lTime: String): String {
        return fromBase64Encoding(payload)
    }

    fun getUnEncryptedPayloadForHistory(payload: String, lTime: String): String {
        return fromBase64Encoding(payload)
    }

    fun getUnEncryptedPayloadForIndoorLocations(payload: String): String {
        return fromBase64Encoding(payload)
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
const val AQI_US_STD_PARAM = "1"
const val AQI_CN_STD_PARAM = "2"