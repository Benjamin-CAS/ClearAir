package com.android_dev.cleanairspaces.utils

interface AsyncResultListener {
    fun onComplete(data: Any? = null, isSuccess: Boolean)
}