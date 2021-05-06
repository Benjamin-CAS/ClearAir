package com.android_dev.cleanairspaces.repositories.api_facing

import android.util.Log
import com.android_dev.cleanairspaces.persistence.api.services.LoggerService
import com.android_dev.cleanairspaces.persistence.local.models.dao.LogsDao
import com.android_dev.cleanairspaces.persistence.local.models.entities.Logs
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepo
@Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val loggerDao: LogsDao,
    private val loggerService: LoggerService
) {
    fun saveLocally(key: String, message: String, isExc: Boolean) {
        coroutineScope.launch(Dispatchers.IO) {
            loggerDao.insertLog(
                Logs(
                    id = 0,
                    key = key,
                    message = message,
                    tag = if (isExc) "Exception" else ""
                )
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
                data.addProperty("tag", "${log.tag}_${log.last_updated}")
                val request = loggerService.sendLogs(
                    data = data
                )
                request.enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        Log.d(
                            "pushLogs onResponse->()", "$response ${response.body()}"
                        )
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Log.d(
                            "pushLogs onFailure->()", "${t.message}"
                        )
                    }

                })
            }
            loggerDao.clearLogData()
        } catch (exc: Exception) {
            Log.d(
                "pushLogs", "${exc.message}", exc
            )
        }

    }
}