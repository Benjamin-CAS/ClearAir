package com.cleanairspaces.android.ui.home.amap

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.amap.api.maps2d.model.BitmapDescriptor
import com.cleanairspaces.android.R
import com.cleanairspaces.android.models.repository.OutDoorLocationsRepo
import com.cleanairspaces.android.utils.MyColorUtils
import com.cleanairspaces.android.utils.UIColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class AMapViewModel @Inject constructor(
    private val locationsRepo: OutDoorLocationsRepo
) : ViewModel() {

    var hasPromptedForLocationSettings = false

    val mapActions = arrayListOf(
        MapActions(action = MapActionChoices.SMART_QR),
        MapActions(action = MapActionChoices.MAP_VIEW),
        MapActions(action = MapActionChoices.ADD),
    )

    fun observeLocations() = locationsRepo.getOutDoorLocationsFlow().asLiveData()

    fun refreshOutDoorLocations(){
        viewModelScope.launch(Dispatchers.IO) {
            locationsRepo.refreshOutDoorLocations()
        }
    }


}

@Parcelize
class MapActions(
    val action: MapActionChoices
): Parcelable

enum class MapActionChoices(val strRes: Int) {
    SMART_QR(strRes = R.string.smart_qr_txt),
    MAP_VIEW(strRes = R.string.map_view_txt),
    ADD(strRes = R.string.add_txt)
}
