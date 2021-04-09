package com.cleanairspaces.android.ui.details

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.cleanairspaces.android.databinding.ActivityLocationDetailsBinding
import com.cleanairspaces.android.ui.BaseActivity
import com.cleanairspaces.android.utils.LocationDetailsInfo
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
        val locationDetailsInfo = intent.getParcelableExtra<LocationDetailsInfo>(INTENT_EXTRA_TAG)
        locationDetailsInfo?.let {
            viewModel.setCustomerDeviceDataDetailed(locationDetailsInfo = locationDetailsInfo)
        }
        observeLocationDetailsInfo()
    }

    override fun handleBackPress() {
        this@LocationDetailsActivity.finish()
    }


    private fun observeLocationDetailsInfo(){
        viewModel.observeLocationDetails().observe(this, Observer {
            if (it != null){
                displayInfo(it)
            }
        })
    }

    private fun displayInfo(locationDetailsInfo: LocationDetailsInfo) {
        val location = locationDetailsInfo.dataDetailed.deviceData
        binding.apply {
            Glide.with(this@LocationDetailsActivity).load(location.getFullLogoUrl())
                .into(compLogo)
            compName.text = location.company
            outInPmTv.text = locationDetailsInfo.locationArea
            pointsTv.text = locationDetailsInfo.outPmValue
            lastUpdateTv.text = locationDetailsInfo.updatedOnTxt
            statusIv.setImageResource(locationDetailsInfo.outStatusIndicatorRes)
            statusTv.text = locationDetailsInfo.outStatusTvTxt
            val txtColor = ContextCompat.getColor(this@LocationDetailsActivity, locationDetailsInfo.bgColor)
            statusTv.setTextColor(txtColor)

        }
    }


}