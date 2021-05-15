package com.android_dev.cleanairspaces.repositories.api_facing

import android.util.Log
import com.android_dev.cleanairspaces.persistence.api.services.LoggerService
import com.android_dev.cleanairspaces.persistence.local.models.dao.LogsDao
import com.android_dev.cleanairspaces.persistence.local.models.entities.Logs
import com.android_dev.cleanairspaces.utils.LAT_LON_DELIMITER
import com.android_dev.cleanairspaces.utils.MyLogger
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepo
@Inject constructor(
        private val loggerDao: LogsDao,
        private val loggerService: LoggerService
) {

    companion object {
        private val TAG = LogRepo::class.java.simpleName
    }

    suspend fun saveLocally(key: String, message: String, isExc: Boolean) {
        try {
            loggerDao.insertLog(
                    Logs(
                            id = 0,
                            key = key,
                            message = message,
                            tag = if (isExc) "Exception" else ""
                    )
            )
        } catch (exc: java.lang.Exception) {
            if (MyLogger.IS_DEBUG_MODE)
                Log.d(
                        TAG, "saveLocally() exc ${exc.message}", exc
                )
        }
    }

    suspend fun pushLogs() {
        try {
            val logs = loggerDao.getLogs()
            for (log in logs) {
                val data = JsonObject()
                data.addProperty("key", log.key)
                data.addProperty("message", log.message)
                data.addProperty("tag", "${log.tag}_${log.recordedAt}")
                val request = loggerService.sendLogs(
                        data = data
                )
                request.enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (MyLogger.IS_DEBUG_MODE)
                            Log.d(
                                    TAG, "pushLogs onResponse->() $response ${response.body()}"
                            )
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        if (MyLogger.IS_DEBUG_MODE)
                            Log.d(
                                    TAG, "pushLogs onFailure->() ${t.message}"
                            )
                    }

                })
            }
            loggerDao.clearLogData()
        } catch (exc: Exception) {
            if (MyLogger.IS_DEBUG_MODE)
                Log.d(
                        TAG, "pushLogs ${exc.message}", exc
                )
        }

    }

    suspend fun updateUserLocation(uniqueID: String, msg: String?) {
        try {
            val lat = msg?.substringBefore(LAT_LON_DELIMITER)
            val lon = msg?.substringAfter(LAT_LON_DELIMITER)
            if (MyLogger.IS_DEBUG_MODE)
                Log.d(
                        TAG, "updateUserLocation for user $uniqueID  to lat_lon $lat $lon"
                )
            if (lat != null && lon != null) {
                val request = loggerService.sendUserLocation(
                        uid = uniqueID, lat = lat, lon = lon
                )
                request.enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (MyLogger.IS_DEBUG_MODE)
                            Log.d(
                                    TAG, "updateUserLocation onResponse->() $response ${response.body()}"
                            )
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        if (MyLogger.IS_DEBUG_MODE)
                            Log.d(
                                    TAG, "updateUserLocation onFailure->() ${t.message}"
                            )
                    }

                })
            }
        } catch (exc: Exception) {
            if (MyLogger.IS_DEBUG_MODE)
                Log.d(
                        TAG, "updateUserLocation for user $uniqueID to lat${LAT_LON_DELIMITER}lon $msg ${exc.message}", exc
                )
        }
    }
}