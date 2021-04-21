package com.android_dev.cleanairspaces.utils

import android.util.Log

object MyLogger {
    private const val DEBUG = true
    fun logThis(tag: String, from: String, msg: String, exc: Exception? = null) {
        if (DEBUG)
            Log.d("CAS_Logger $tag", "$from $msg", exc)
    }
}