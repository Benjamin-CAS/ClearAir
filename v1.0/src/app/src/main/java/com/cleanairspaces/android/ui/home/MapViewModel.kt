package com.cleanairspaces.android.ui.home

import android.location.Location
import android.os.Parcelable
import androidx.lifecycle.*
import com.amap.api.maps.model.LatLng as aLatLng
import com.cleanairspaces.android.R
import com.cleanairspaces.android.models.entities.OutDoorLocations
import com.cleanairspaces.android.models.repository.OutDoorLocationsRepo
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.OUTDOOR_LOCATIONS_REFRESH_RATE_MILLS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationsRepo: OutDoorLocationsRepo
) : ViewModel() {

    init {
        refreshOutDoorLocations()
    }


    /*********** AMAP SPECIFIC LOCATION *************/
    private var userLastKnowALatLng: aLatLng? = null
    fun setUserLastKnownALatLng(it: Location?) {
        it?.let {
            if(it.latitude != 0.0 && it.longitude != 0.0) {
                userLastKnowALatLng = aLatLng(it.latitude, it.longitude)
            }
            MyLogger.logThis(TAG, "userLastKnownLocale()" , "-- $it")
        }
    }
    fun getUserLastKnownALatLng(): aLatLng? = userLastKnowALatLng


    var hasSetMyLocationStyle = false
    val mapActions = arrayListOf(
        MapActions(action = MapActionChoices.SMART_QR),
        MapActions(action = MapActionChoices.MAP_VIEW),
        MapActions(action = MapActionChoices.ADD),
    )

    fun observeLocations(): LiveData<List<OutDoorLocations>> =
        locationsRepo.getOutDoorLocationsLive().asLiveData()

    private fun refreshOutDoorLocations() {
        MyLogger.logThis(TAG, "refreshOutDoorLocations()", "called --")
        viewModelScope.launch(Dispatchers.IO) {
            locationsRepo.refreshOutDoorLocations()
            delay(OUTDOOR_LOCATIONS_REFRESH_RATE_MILLS)
            withContext(Dispatchers.Main) {
                refreshOutDoorLocations()
            }
        }
    }




    companion object {
        private val TAG = MapViewModel::class.java.simpleName
    }
}

@Parcelize
class MapActions(
    val action: MapActionChoices
) : Parcelable

enum class MapActionChoices(val strRes: Int) {
    SMART_QR(strRes = R.string.smart_qr_txt),
    MAP_VIEW(strRes = R.string.map_view_txt),
    ADD(strRes = R.string.add_txt)
}
