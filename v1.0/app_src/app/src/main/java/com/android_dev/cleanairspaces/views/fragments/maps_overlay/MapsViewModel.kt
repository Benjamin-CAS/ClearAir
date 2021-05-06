package com.android_dev.cleanairspaces.views.fragments.maps_overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.amap.api.maps.model.Marker
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val appDataRepo: AppDataRepo,
    private val myLogger: MyLogger
) : ViewModel() {

    private val TAG = MapsViewModel::class.java.simpleName

    var myLocMarkerOnAMap: Marker? = null
    var myLocMarkerOnGMap: com.google.android.gms.maps.model.Marker? = null
    var alreadyPromptedUserForGPS = false

    fun observeMapLang() = dataStoreManager.getMapLang().asLiveData()
    fun observeSelectedAqiIndex() = dataStoreManager.getAqiIndex().asLiveData()


    fun observeMapData() = appDataRepo.getMapDataFlow().asLiveData()
    fun observeWatchedLocations() = appDataRepo.getWatchedLocationHighLights().asLiveData()
    fun deleteLocation(location: WatchedLocationHighLights) {
        appDataRepo.stopWatchingALocation(location)
    }

}