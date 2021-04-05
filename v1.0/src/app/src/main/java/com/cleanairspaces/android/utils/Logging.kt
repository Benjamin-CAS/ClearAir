package com.cleanairspaces.android.utils

import android.util.Log

object MyLogger {
    private const val DEBUG = true
    fun logThis(
        from: String,
        at: String,
        message: String? = "no message",
        exception: Exception? = null
    ) {
        if (DEBUG) {
            Log.d("CAS_LOGS $from", "at $at msg : $message", exception)
        }
    }


}