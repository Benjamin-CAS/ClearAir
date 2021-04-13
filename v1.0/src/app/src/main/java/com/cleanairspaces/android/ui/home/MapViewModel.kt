package com.cleanairspaces.android.ui.home

import android.location.Location
import android.os.Parcelable
import androidx.lifecycle.*
import com.cleanairspaces.android.R
import com.cleanairspaces.android.models.entities.LocationDetailsGeneralDataWrapper
import com.cleanairspaces.android.models.entities.MyLocationDetails
import com.cleanairspaces.android.models.entities.OutDoorLocations
import com.cleanairspaces.android.models.repository.OutDoorLocationsRepo
import com.cleanairspaces.android.models.repository.ScannedDevicesRepo
import com.cleanairspaces.android.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import com.amap.api.maps.model.LatLng as aLatLng

@HiltViewModel
class MapViewModel @Inject constructor(
        private val locationsRepo: OutDoorLocationsRepo,
        private val scannedDevicesRepo: ScannedDevicesRepo,
        private val dataStoreManager: DataStoreManager
) : ViewModel() {

    init {
        refreshOutDoorLocations()
    }


    fun getSelectedAqiIndex(): LiveData<String?> = dataStoreManager.getAqiIndex().asLiveData()

    /******* my locations **********/
    private val myLocationDetailsLive = MutableLiveData<List<LocationDetailsGeneralDataWrapper>>(
            arrayListOf()
    )

    fun refreshMyLocationsFlow(): LiveData<List<MyLocationDetails>> {
        return scannedDevicesRepo.getMyLocations().asLiveData()
    }


    fun updateMyLocationsDetails(myLocations: List<MyLocationDetails>) {
        viewModelScope.launch(Dispatchers.IO) {
            val myLocationDetails = arrayListOf<LocationDetailsGeneralDataWrapper>()
            if (myLocations.isNotEmpty()) {
                for (locationDetails in myLocations) {
                    val foundCustomerDeviceData = scannedDevicesRepo.getMyDeviceBy(
                            compId = locationDetails.company_id,
                            locId = locationDetails.location_id
                    )
                    if (foundCustomerDeviceData.isNotEmpty()) {
                        myLocationDetails.add(
                                LocationDetailsGeneralDataWrapper(
                                        locationDetails = locationDetails,
                                        generalData = foundCustomerDeviceData[0]
                                )
                        )
                    }
                }
            }
            withContext(Dispatchers.Main) {
                MyLogger.logThis(TAG, "updateMyLocationsDetails()", "called")
                myLocationDetailsLive.value = myLocationDetails
                if (myLocations.isNotEmpty()) {
                    //TODO refresh startServeRefreshCallsForMyLocations(myLocations = myLocations)
                }
            }
        }
    }

    private lateinit var locationsToRefresh : List<MyLocationDetails>
    private fun startServeRefreshCallsForMyLocations(myLocations: List<MyLocationDetails>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (myLocation in myLocations) {
                val timeStamp = System.currentTimeMillis().toString()
                val compId = myLocation.company_id
                val locId = myLocation.location_id
                val userName = myLocation.lastKnownUserName
                val userPassword = myLocation.lastKnownPassword
                val pl = QrCodeProcessor.getEncryptedEncodedPayloadForDeviceDetails(
                        compId = compId,
                        locId = locId,
                        userName = userName,
                        userPassword = userPassword,
                        timeStamp = timeStamp
                )
                scannedDevicesRepo.fetchDataForMyLocation(
                        compId = compId,
                        locId = locId,
                        payload = pl,
                        ltime = timeStamp,
                        userName = userName,
                        userPassword = userPassword,
                )
            }

            delay(INDOOR_LOCATIONS_REFRESH_RATE_MILLS)
            locationsToRefresh = myLocations
            withContext(Dispatchers.Main) {
                startServeRefreshCallsForMyLocations(locationsToRefresh)
            }
        }
    }

    fun observeMyLocationDetails(): LiveData<List<LocationDetailsGeneralDataWrapper>> = myLocationDetailsLive

    /*********** AMAP SPECIFIC LOCATION *************/
    private var userLastKnowALatLng: aLatLng? = null
    fun setUserLastKnownALatLng(it: Location?) {
        it?.let {
            userLastKnowALatLng = if (it.latitude != 0.0 && it.longitude != 0.0) {
                aLatLng(it.latitude, it.longitude)
            } else {
                //todo remove in production
                aLatLng(SHANGHAI_LAT, SHANGHAI_LON)
            }
            MyLogger.logThis(TAG, "userLastKnownLocale()", "-- $it")
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

const val SHANGHAI_LAT: Double = 31.224361
const val SHANGHAI_LON: Double = 121.469170