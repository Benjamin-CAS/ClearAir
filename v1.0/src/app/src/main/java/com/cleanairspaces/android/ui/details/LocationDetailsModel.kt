package com.cleanairspaces.android.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cleanairspaces.android.models.repository.ScannedDevicesRepo
import com.cleanairspaces.android.utils.MyLocationDetailsWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationDetailsModel @Inject constructor(
        private val scannedDevicesRepo: ScannedDevicesRepo
) : ViewModel() {

    private val locationDetailsInfoLive = MutableLiveData<MyLocationDetailsWrapper>()
    fun setCustomerDeviceDataDetailed(
            myLocationDetailsWrapper: MyLocationDetailsWrapper
    ) {
        locationDetailsInfoLive.value = myLocationDetailsWrapper
    }

    fun observeLocationDetails(): LiveData<MyLocationDetailsWrapper> = locationDetailsInfoLive
}
