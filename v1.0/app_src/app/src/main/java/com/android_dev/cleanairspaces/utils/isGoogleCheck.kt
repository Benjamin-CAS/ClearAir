package com.android_dev.cleanairspaces.utils

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

var isCheckGooglePlay = false



fun <T>requestFlow(
    request:suspend () -> T,
    resultState:MutableLiveData<T>,
    showLoading:Boolean = false,
    loadingMsg:String = "网络请求中"
):Flow<T>{
    return flow {

    }
}