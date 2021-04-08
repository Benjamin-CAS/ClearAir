package com.cleanairspaces.android.ui.settings

import androidx.lifecycle.*
import com.cleanairspaces.android.utils.DataStoreManager
import com.cleanairspaces.android.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsActivityViewModel @Inject constructor(
        private val dataStoreManager: DataStoreManager
    ) : ViewModel() {

    private val TAG = SettingsActivityViewModel::class.java.simpleName

    fun setSelectedAqi(selectedAqi: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.saveAqiIndex(newAqiIndex = selectedAqi)
        }
    }
    fun getSelectedAqi() : LiveData<String?> = dataStoreManager.getAqiIndex().asLiveData()


}