package com.android_dev.cleanairspaces.utils

import android.util.Log
import com.android_dev.cleanairspaces.repositories.api_facing.LogRepo
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyLogger @Inject constructor(
    private val logRepo: LogRepo
) {


    val uniqueID = UUID.randomUUID().toString()

    suspend fun logThis(tag: LogTags, from: String, msg: String? = "", exc: Exception? = null) {

        if (IS_DEBUG_MODE) {
            Log.d("CAS_Logger ${tag.readableMsg}", "User $uniqueID $from $msg", exc)
        } else {
            if (tag == LogTags.USER_LOCATION_CHANGED) {
                logRepo.updateUserLocation(uniqueID, msg)
            }
            logRepo.saveLocally(tag.readableMsg, "User $uniqueID $from $msg", isExc = (exc != null))
        }
    }


    companion object {
        const val IS_DEBUG_MODE = false
    }
}

enum class LogTags(val readableMsg: String) {
    EXCEPTION(readableMsg = "exception"),
    USER_ACTION_OPEN_SCREEN(readableMsg = "user action | open screen"),
    USER_ACTION_CLICK_FEATURE(readableMsg = "user action | click feature"),
    USER_ACTION_SEARCH(readableMsg = "user action | search"),
    USER_ACTION_OPEN_APP(readableMsg = "user action | open app"),
    USER_ACTION_CLOSE_APP(readableMsg = "user action | close app"),
    USER_LOCATION_CHANGED(readableMsg = "user location changed"),
    USER_ACTION_SETTINGS(readableMsg = "user changed settings")
}