package com.android_dev.cleanairspaces.bg_work

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(private val myLogger: MyLogger): ViewModel() {

    fun logeS() = viewModelScope.launch(Dispatchers.IO) {
        myLogger.logThis(LogTags.USER_ACTION_SETTINGS,"MyViewModel","执行打印结果")
    }
    companion object {
        const val TAG = "MyViewModel"
    }
}