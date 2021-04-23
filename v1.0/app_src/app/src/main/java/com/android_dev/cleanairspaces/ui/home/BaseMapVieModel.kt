package com.android_dev.cleanairspaces.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.amap.api.maps.model.Marker as AMarker
import com.google.android.gms.maps.model.Marker as GMarker


@HiltViewModel
class BaseMapVieModel @Inject constructor(
        private val dataStoreManager: DataStoreManager,
        private val appDataRepo: AppDataRepo
) : ViewModel() {

    var myLocMarkerOnAMap: AMarker? = null
    var myLocMarkerOnGMap: GMarker? = null
    var alreadyPromptedUserForGPS = false

    fun observeMapLang() = dataStoreManager.getMapLang().asLiveData()
    fun observeMapData() = appDataRepo.getMapDataFlow().asLiveData()
    fun observeSelectedAqiIndex() = dataStoreManager.getAqiIndex().asLiveData()
    fun observeWatchedLocations() = appDataRepo.getWatchedLocationHighLights().asLiveData()
}

