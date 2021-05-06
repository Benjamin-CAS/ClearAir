package com.android_dev.cleanairspaces.utils

import android.util.Log
import com.android_dev.cleanairspaces.repositories.api_facing.LogRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyLogger @Inject constructor(
    private val logRepo: LogRepo
) {
    fun logThis(tag: String, from: String, msg: String, exc: Exception? = null) {
        if (Companion.DEBUG)
            Log.d("CAS_Logger $tag", "$from $msg", exc)
        else {
            logRepo.saveLocally("CAS_Logger $tag", "$from $msg", isExc = (exc != null))
        }
    }

    companion object {
        private const val DEBUG = true
    }
}