package com.android_dev.cleanairspaces.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    fun observeAQIIndex() = dataStoreManager.getAqiIndex().asLiveData()
    fun observeSelectedMapLang() = dataStoreManager.getMapLang().asLiveData()
    fun observeSelectedMap() = dataStoreManager.getSelectedMap().asLiveData()


    fun setAQIIndex(selectedAqi: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreManager.saveAqiIndex(newAqiIndex = selectedAqi)
    }

    fun setSelectedMap(selectedMap: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreManager.saveMap(selectedMap = selectedMap)
    }

    fun setSelectedMapLang(selectedMapLang: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreManager.saveMapLang(selectedMapLang)
    }


}