package com.android_dev.cleanairspaces.views.fragments.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val myLogger: MyLogger
) : ViewModel() {

    private val TAG = SettingsViewModel::class.java.simpleName

    fun observeAQIIndex() = dataStoreManager.getAqiIndex().asLiveData()
    fun observeSelectedMapLang() = dataStoreManager.getMapLang().asLiveData()
    fun observeSelectedMap() = dataStoreManager.getSelectedMap().asLiveData()


    fun setAQIIndex(selectedAqi: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreManager.saveAqiIndex(newAqiIndex = selectedAqi)
        viewModelScope.launch(Dispatchers.IO) {
            myLogger.logThis(
                tag = LogTags.USER_ACTION_SETTINGS,
                from = TAG,
                msg = "changed AQI to $selectedAqi"
            )
        }
    }

    fun setSelectedMap(selectedMap: String) = viewModelScope.launch(Dispatchers.IO) {

        dataStoreManager.saveMap(selectedMap = selectedMap)

        viewModelScope.launch(Dispatchers.IO) {
            myLogger.logThis(
                tag = LogTags.USER_ACTION_SETTINGS,
                from = TAG,
                msg = "changed map type to $selectedMap"
            )
        }
    }

    fun setSelectedMapLang(selectedMapLang: String) = viewModelScope.launch(Dispatchers.IO) {

        dataStoreManager.saveMapLang(selectedMapLang)

        viewModelScope.launch(Dispatchers.IO) {
            myLogger.logThis(
                tag = LogTags.USER_ACTION_SETTINGS,
                from = TAG,
                msg = "changed map language to $selectedMapLang"
            )
        }
    }

}