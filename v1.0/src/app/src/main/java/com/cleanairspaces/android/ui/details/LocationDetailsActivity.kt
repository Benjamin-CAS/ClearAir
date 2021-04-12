package com.cleanairspaces.android.ui.details

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityLocationDetailsBinding
import com.cleanairspaces.android.databinding.LocationDetailsInOutLayoutBinding
import com.cleanairspaces.android.ui.BaseActivity
import com.cleanairspaces.android.utils.MyLocationDetailsWrapper
import com.cleanairspaces.android.utils.MyLogger
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LocationDetailsActivity : BaseActivity() {
    companion object {
        private val TAG = LocationDetailsActivity::class.java.simpleName
        val INTENT_EXTRA_TAG = "locationDetails"
    }


    private lateinit var binding: ActivityLocationDetailsBinding

    private val viewModel: LocationDetailsModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationDetailsBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        //toolbar
        super.setToolBar(binding.toolbarLayout, isHomeAct = false)
        val locationDetailsInfo = intent.getParcelableExtra<MyLocationDetailsWrapper>(INTENT_EXTRA_TAG)
        locationDetailsInfo?.let {
            viewModel.setCustomerDeviceDataDetailed(myLocationDetailsWrapper = locationDetailsInfo)
        }
        observeLocationDetailsInfo()
    }

    override fun handleBackPress() {
        this@LocationDetailsActivity.finish()
    }


    private fun observeLocationDetailsInfo(){
        viewModel.observeLocationDetails().observe(this, Observer { myLocationDetailsWrapper ->
            if (myLocationDetailsWrapper != null){
                val generalData = myLocationDetailsWrapper.wrappedData.generalData
                val locationDetailsInfo = myLocationDetailsWrapper.wrappedData.locationDetails
                binding.apply {
                    Glide.with(this@LocationDetailsActivity).load(generalData.getFullLogoUrl())
                        .into(compLogo)
                    compName.text = generalData.company
                    if (locationDetailsInfo.indoor.indoor_pm.isBlank()){
                        //we only have outdoor info
                        displayOutDoorInfo(myLocationDetailsWrapper)
                    }else{
                        displayIndoorInfo(myLocationDetailsWrapper)
                    }
                }
            }
        })
    }

    private fun displayIndoorInfo(
        uiReadyPmInfo: MyLocationDetailsWrapper) {
        val root = binding.pmInfoContainer
        val view : View = root.findViewById(R.id.in_out_door_layout)
        view.isVisible = true
        val inOutLayoutBinding = LocationDetailsInOutLayoutBinding.bind(view)
        inOutLayoutBinding.apply {
            indoorInfo.setBackgroundResource(uiReadyPmInfo.inBgTransparent)
            outdoorInfo.setBackgroundResource(uiReadyPmInfo.outBgTransparent)
            indoorPmValueTv.text = uiReadyPmInfo.inPmValue
            outdoorPmValueTv.text = uiReadyPmInfo.outPmValue
            indoorPmStatusIv.setImageResource(uiReadyPmInfo.inStatusIndicatorRes)
            outdoorPmStatusIv.setImageResource(uiReadyPmInfo.outStatusIndicatorRes)
            indoorPmStatusTv.text = uiReadyPmInfo.inStatusTvTxt
            outdoorPmStatusTv.text = uiReadyPmInfo.outStatusTvTxt
            updatedOnTv.text = uiReadyPmInfo.updatedOnTxt
            outdoorLocation.text = uiReadyPmInfo.locationArea
            indoorPmIndex.text = getString(R.string.default_pm_index_value)
            indoorPmIndexVal.text = uiReadyPmInfo.ogInPmTxt
            indoorTmpValue.text = uiReadyPmInfo.tmpLvl
            indoorTvocVal.text = uiReadyPmInfo.vocLvlTxt
            indoorCoVal.text = uiReadyPmInfo.co2LvlTxt
            indoorHumidityVal.text = uiReadyPmInfo.humidLvl
            indoorPmIndexGradient.isEnabled = false
            indoorPmIndexGradient.max = uiReadyPmInfo.pmSliderMax
            indoorPmIndexGradient.progress = uiReadyPmInfo.pmSliderValue
            indoorPmIndexGradient.thumb = ContextCompat.getDrawable(this@LocationDetailsActivity, uiReadyPmInfo.pmSliderDiskRes)

        }
    }

    private fun displayOutDoorInfo(
        uiReadyInfo: MyLocationDetailsWrapper
    ){

    }

}