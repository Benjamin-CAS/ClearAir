package com.android_dev.cleanairspaces.views.fragments.details_tabbed.details

import androidx.lifecycle.*
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.persistence.local.models.entities.*
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel
@Inject constructor(
    private val dataStoreManager: DataStoreManager,
        private val repo: AppDataRepo,
) : ViewModel() {

    var aqiIndex: String? = null

    private val TAG = DetailsViewModel::class.java.simpleName

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.getAqiIndex().collectLatest {
                withContext(Dispatchers.Main) {
                    aqiIndex = it
                }
            }
        }
    }

    fun observeWatchedLocation(): LiveData<WatchedLocationHighLights> = repo.watchedLocationHighLights


}