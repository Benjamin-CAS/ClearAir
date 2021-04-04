package com.cleanairspaces.android.utils

import android.util.Base64
import android.util.Base64.decode
import android.util.Base64.encodeToString
import com.cleanairspaces.android.R
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.LOCATION_INFO_METHOD_FOR_KEY
import com.cleanairspaces.android.models.api.QrScannedItemsApiService.Companion.MONITOR_INFO_METHOD_FOR_KEY

/*
**
//example monitor qrContent == http://monitor.cleanairspaces.com/downloadApp?LOCIDBHBXGEGYCEIZ
//or device qrContent ==  msg : qrContent NATDF483FDA0DBCB000000
 */
object QrCodeProcessor {

    private val TAG = QrCodeProcessor::class.java.simpleName
    private const val QR_LOCATION_ERR  =  R.string.parsing_device_qr_failed
    private const val QR_UNKNOWN_ERR  =R.string.scan_qr_code_unknown
    private const val SUCCESS = 200

    fun identifyQrCode(qrContent : String?): ParserResult {
        when {
            qrContent.isNullOrBlank() -> return ParserResult(codeRes = QR_UNKNOWN_ERR)
            qrContent.contains(DEFAULT_QR_LOCATION_ID) -> {
                val startIndexOfLocIdentifier = qrContent.indexOf(DEFAULT_QR_LOCATION_ID)
                val lastIndexOfLocIdentifier = (startIndexOfLocIdentifier + DEFAULT_QR_LOCATION_ID.length) - 1
                if (startIndexOfLocIdentifier == -1 || lastIndexOfLocIdentifier == -1) return ParserResult(codeRes = QR_LOCATION_ERR)

                val firstIndexOfLocId =
                    qrContent.indexOf(DEFAULT_QR_LOCATION_ID_L_PAD, startIndex = lastIndexOfLocIdentifier) + 1
                val lastIndexOfLocId =
                    qrContent.indexOf(DEFAULT_QR_LOCATION_ID_R_PAD, startIndex = lastIndexOfLocIdentifier)

                if (firstIndexOfLocId == -1 || lastIndexOfLocIdentifier == -1) return ParserResult(codeRes = QR_LOCATION_ERR)


                val companyId =
                    qrContent.substring(startIndex = lastIndexOfLocIdentifier + 1, endIndex = firstIndexOfLocId - 1)

                val locationId =
                    qrContent.substring(startIndex = firstIndexOfLocId, endIndex = lastIndexOfLocId)

                if (locationId.isEmpty() || companyId.isEmpty()) return ParserResult(codeRes = QR_LOCATION_ERR)

                //we have a working location id
                val asciiViableLocId =  getProperAsciiCheckedIds(locationId)
                val asciiViableCompanyId =  getProperAsciiCheckedIds(companyId)
                val asciiViableLocIdAsInt =  asciiViableLocId.toInt()
                val asciiViableCompIdAsInt =  asciiViableCompanyId.toInt()
                if (asciiViableLocIdAsInt == 0 || asciiViableCompIdAsInt== 0 )
                    return ParserResult(codeRes = QR_LOCATION_ERR)

                MyLogger.logThis(
                    TAG, "identifyQrCode($qrContent : String)",
                            "locationId $locationId companyId $companyId " +
                            "locAsInt $asciiViableLocIdAsInt compAsInt $asciiViableCompIdAsInt"
                )
                return ParserResult(codeRes = SUCCESS, locId = asciiViableLocIdAsInt, compId = asciiViableCompIdAsInt, extraData = "company ID $companyId @location ID $locationId")
            }
            qrContent.length == QR_MONITOR_ID_PADDED_LENGTH || qrContent.length == QR_MONITOR_ID_TRUE_LENGTH -> {
                // This is a monitorID, let's get the location and try to add it
                val properMonitorId  =
                if (qrContent.length == QR_MONITOR_ID_PADDED_LENGTH){
                   qrContent.substring(QR_MONITOR_ID_PAD_LENGTH until QR_MONITOR_ID_PADDED_LENGTH  - QR_MONITOR_ID_PAD_LENGTH)
                }else qrContent

                return ParserResult(codeRes = SUCCESS, monitorId = properMonitorId,  extraData = properMonitorId)
            }
            else -> return ParserResult(codeRes = QR_UNKNOWN_ERR)
        }
    }

    private fun getProperAsciiCheckedIds(strId: String): String {
        var asciiViableId = ""
        for(char in strId)
        {
            val charAsInt = char.toInt()
            if (charAsInt in 65..73)
            {

                val c = (charAsInt - 17).toChar()
                asciiViableId += c
            }

        }
        return asciiViableId
    }


    private fun calculateEncKey(payload : String, key : String): String {
        var trueKey = ""
        val payLoadLength = payload.length
        while (trueKey.length < payLoadLength){
            trueKey += key
        }
        return trueKey.substring(0,payload.length)
    }

    private fun doCASEncryptOrDecrypt(payload : String, key: String) : String{
        val trueKey = calculateEncKey(payload, key)
        var encodedStr = ""
        for ((index,letter) in payload.withIndex()){
            val keyForCurrentPos = trueKey[index]
            val code = (keyForCurrentPos.toInt()).xor(letter.toInt())
            encodedStr += if (code < 32 || code  > 126){
                letter
            } else code.toChar()
        }
        return encodedStr
    }

    private fun toBase64Encoding(encryptedPayload : String): String {
       return encodeToString(encryptedPayload.encodeToByteArray(), Base64.NO_PADDING).trim()
    }

    private fun fromBase64Encoding(base64EncodeStr : String): String {
        return decode(base64EncodeStr.encodeToByteArray(), Base64.NO_PADDING).decodeToString().trim()
    }

    private fun getProperPayload(locId: Int, compId: Int):String {
        return "{\"$COMP_ID_KEY\":$compId, \"$LOC_ID_KEY\":$locId}"
    }


    private fun getProperPayloadKeyForCompLocation(timeStamp: String): String {
        return "${LOCATION_INFO_METHOD_FOR_KEY}$timeStamp"
    }

    private fun getProperPayloadKeyForMonitor(timeStamp: String): String {
        return "${MONITOR_INFO_METHOD_FOR_KEY}$timeStamp"
    }

    fun getEncryptedEncodedPayloadForLocation(locId: Int, compId: Int, timeStamp: String): String {
        val key = getProperPayloadKeyForCompLocation(timeStamp)
        MyLogger.logThis(TAG, "getEncryptedEncodedPayload($locId : L, $compId : C, $timeStamp : t)", "true key $key")
        val payload = getProperPayload(locId, compId)
        MyLogger.logThis(TAG, "getEncryptedEncodedPayload()", "payload $payload")
        val casEncrypted = doCASEncryptOrDecrypt(payload = payload, key= key)
        MyLogger.logThis(TAG, "getEncryptedEncodedPayload()", "encrypted $casEncrypted")
        return toBase64Encoding(casEncrypted)
    }

    fun getEncryptedEncodedPayloadForMonitor(monitorId: String, timeStamp: String): String {
        val key = getProperPayloadKeyForMonitor(timeStamp)
        MyLogger.logThis(TAG, "getEncryptedEncodedPayloadForMonitor(monitorId: $monitorId, $timeStamp : time)", "true key $key")
        val casEncrypted = doCASEncryptOrDecrypt(payload = monitorId, key= key)
        MyLogger.logThis(TAG, "getEncryptedEncodedPayloadForMonitor()", "encrypted $casEncrypted")
        return toBase64Encoding(casEncrypted)
    }

    fun getUnEncryptedPayload(encPayload: String, lTime: String, forCompLocation: Boolean): String {
        val decoded = fromBase64Encoding(encPayload)
        val key = if (forCompLocation)  getProperPayloadKeyForCompLocation(lTime)
                else getProperPayloadKeyForMonitor(lTime)
        val casDecrypted = doCASEncryptOrDecrypt(payload = decoded, key= key)
        MyLogger.logThis(TAG, "getUnEncryptedPayload...", "decrypted $casDecrypted")
        return casDecrypted
    }




}

data class ParserResult(
    val codeRes : Int,
    val locId : Int? = null,
    val compId : Int? = null,
    val monitorId : String? = null,
    val extraData : String = ""
)
