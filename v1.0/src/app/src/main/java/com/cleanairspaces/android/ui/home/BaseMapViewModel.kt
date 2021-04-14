package com.cleanairspaces.android.ui.home

import android.location.Location
import android.os.Parcelable
import androidx.lifecycle.*
import com.cleanairspaces.android.R
import com.cleanairspaces.android.models.entities.LocationDetailsGeneralDataWrapper
import com.cleanairspaces.android.models.entities.LocationDetails
import com.cleanairspaces.android.models.entities.OutDoorLocations
import com.cleanairspaces.android.models.repository.OutDoorLocationsRepo
import com.cleanairspaces.android.models.repository.ScannedDevicesRepo
import com.cleanairspaces.android.utils.DataStoreManager
import com.cleanairspaces.android.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import com.amap.api.maps.model.LatLng as aLatLng

@HiltViewModel
class BaseMapViewModel @Inject constructor(
    private val locationsRepo: OutDoorLocationsRepo,
    private val scannedDevicesRepo: ScannedDevicesRepo,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    //listening to the aqi-index in settings
    fun getSelectedAqiIndex(): LiveData<String> = dataStoreManager.getAqiIndex().asLiveData()
    fun getSelectedMapLang(): LiveData<String> = dataStoreManager.getMapLang().asLiveData()

    //observing my locations
    fun refreshMyLocationsFlow() = scannedDevicesRepo.getMyLocations().asLiveData()

    fun observeOutDoorLocations(): LiveData<List<OutDoorLocations>> =
        locationsRepo.getOutDoorLocationsLive().asLiveData()


    /*
      *** wraps data from 2 entity classes for ui purposes
       */
    private val myLocationDetailsWrapperLive =
        MutableLiveData<List<LocationDetailsGeneralDataWrapper>>(
            arrayListOf()
        )

    /*
    *** appends the general data obtained from scanning a device with the details in myLocations class
     */
    fun updateMyLocationsDetailsWrapper(locations: List<LocationDetails>) {
        viewModelScope.launch(Dispatchers.IO) {
            val myLocationDetails = arrayListOf<LocationDetailsGeneralDataWrapper>()
            if (locations.isNotEmpty()) {
                for (locationDetails in locations) {
                    val foundCustomerDeviceData = scannedDevicesRepo.getMyDeviceBy(
                        compId = locationDetails.company_id,
                        locId = locationDetails.location_id
                    )
                    if (foundCustomerDeviceData.isNotEmpty()) {
                        myLocationDetails.add(
                            LocationDetailsGeneralDataWrapper(
                                locationDetails = locationDetails,
                                generalDataFromQr = foundCustomerDeviceData[0]
                            )
                        )
                    }
                }
            }
            withContext(Dispatchers.Main) {
                myLocationDetailsWrapperLive.value = myLocationDetails
            }
        }
    }

    fun observeMyLocationDetailsWrapper(): LiveData<List<LocationDetailsGeneralDataWrapper>> =
        myLocationDetailsWrapperLive

    /*********** A-MAPS SPECIFIC LOCATION *************/
    private var userLastKnowALatLng: aLatLng? = null
    fun setUserLastKnownALatLng(it: Location?) {
        it?.let {
            userLastKnowALatLng = if (it.latitude != 0.0 && it.longitude != 0.0) {
                //todo saveInDataBase() and then schedule work logger
                aLatLng(it.latitude, it.longitude)
            } else null
            MyLogger.logThis(TAG, "userLastKnownLocale()", "-- $it")
        }
    }

    fun getUserLastKnownALatLng(): aLatLng? = userLastKnowALatLng

    /*TODO********** G-MAPS SPECIFIC LOCATION *************/


    var hasSetMyLocationStyle = false
    val mapActions = arrayListOf(
        MapActions(action = MapActionChoices.SMART_QR),
        MapActions(action = MapActionChoices.MAP_VIEW),
        MapActions(action = MapActionChoices.ADD),
    )


    companion object {
        private val TAG = BaseMapViewModel::class.java.simpleName
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

const val SHANGHAI_LAT: Double = 31.224361
const val SHANGHAI_LON: Double = 121.469170