package com.cleanairspaces.android.models.repository

import androidx.lifecycle.MutableLiveData
import com.amap.api.maps2d.model.LatLng
import com.cleanairspaces.android.R
import com.cleanairspaces.android.models.api.LocationsApi
import com.cleanairspaces.android.models.entities.LocationInfo
import com.cleanairspaces.android.models.entities.LocationStatus


class LocationsRepo : LocationsApi {

    private val locationUpdates = MutableLiveData<List<LocationInfo>>()
    fun getLocations(): MutableLiveData<List<LocationInfo>> {
        if (locationUpdates.value.isNullOrEmpty())
            observeLocations()
        return locationUpdates
    }

    fun getLocationByName(locationName : String): LocationInfo? {
        return if (locationUpdates.value.isNullOrEmpty()) null
        else {
            val locations  =  locationUpdates.value!!
            locations.find { it.location_name == locationName }
        }
    }

    override fun observeLocations() {
        val fetchedLocationUpdates = arrayListOf<LocationInfo>(
                LocationInfo(
                        location_name = "Saint Gobain",
                        updated = System.currentTimeMillis(),
                        location_area = "Outdoor Shanghai",
                        indoor_pm = "PM2.5",
                        indoor_points = 6,
                        indoor_status = LocationStatus.DANGER,
                        outdoor_pm = "PM2.5",
                        outdoor_points = 18,
                        outdoor_status = LocationStatus.DANGER,
                        location = LatLng(31.2271589,121.4396738),
                        locationLogoRes = R.drawable.saint_gobain_logo
                ),
                LocationInfo(
                        location_name = "Fudan University",
                        updated = System.currentTimeMillis(),
                        location_area = "Outdoor Shanghai",
                        indoor_pm = "PM4.5",
                        indoor_points = 8,
                        indoor_status = LocationStatus.MODERATE,
                        outdoor_pm = "PM4.5",
                        outdoor_points = 22,
                        outdoor_status = LocationStatus.MODERATE,
                        location = LatLng(31.2182884,121.4191865),
                        locationLogoRes = R.drawable.fudan_university_logo
                ),
                LocationInfo(
                        location_name = "Changning District",
                        updated = System.currentTimeMillis(),
                        location_area = "Outdoor Shanghai",
                        indoor_pm = "PM7.5",
                        indoor_points = 9,
                        indoor_status = LocationStatus.SAFE,
                        outdoor_pm = "PM9.5",
                        outdoor_points = 26,
                        outdoor_status = LocationStatus.SAFE,
                        location = LatLng(31.2109503,121.3506141)
                )
        )
        locationUpdates.value = fetchedLocationUpdates
    }

}