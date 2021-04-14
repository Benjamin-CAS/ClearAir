package com.cleanairspaces.android.ui.details.overview_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.FragmentDetailsBinding
import com.cleanairspaces.android.databinding.LocationDetailsInOutLayoutBinding
import com.cleanairspaces.android.databinding.LocationDetailsOutLayoutBinding
import com.cleanairspaces.android.models.entities.LocationDetails
import com.cleanairspaces.android.ui.details.LocationDetailsActivity
import com.cleanairspaces.android.ui.details.LocationDetailsViewModel
import com.cleanairspaces.android.utils.AQI
import com.cleanairspaces.android.utils.MyLocationDetailsWrapper
import com.cleanairspaces.android.utils.UNSET_PARAM_VAL
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetailsFragment : Fragment() {

    companion object {
        private val TAG = DetailsFragment::class.java.simpleName
    }

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationDetailsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLocationDetailsInfo()
    }

    private fun disableSliders(inOutLayoutBinding: LocationDetailsInOutLayoutBinding) {
        inOutLayoutBinding.apply {
            indoorPmIndexGradient.isEnabled = false
            indoorCoGradient.isEnabled = false
            indoorTmpGradient.isEnabled = false
            indoorTvocGradient.isEnabled = false
        }
    }


    private fun observeLocationDetailsInfo() {
        viewModel.observeLocationDetails().observe(viewLifecycleOwner, Observer { myLocationDetailsWrapper ->
            if (myLocationDetailsWrapper != null) {
                val generalData = myLocationDetailsWrapper.wrappedData.generalDataFromQr
                val locationDetailsInfo = myLocationDetailsWrapper.wrappedData.locationDetails
                binding.apply {
                    Glide.with(requireContext()).load(generalData.getFullLogoUrl())
                        .into(compLogo)
                    compName.text = generalData.company
                    progressCircular.isVisible = false
                }
                if (myLocationDetailsWrapper.indoorPmValue == UNSET_PARAM_VAL) {
                    displayOutDoorInfo(myLocationDetailsWrapper)
                } else {
                    displayIndoorInfo(myLocationDetailsWrapper)
                }
                displayRecommendations(locationDetailsInfo.outdoor.outdoor_pm)
            } else {
                binding.progressCircular.isVisible = true
            }
        })
    }

    private fun displayRecommendations(outDoorPm: String) {
        var hasRecommendations = false
        if (outDoorPm.isNotBlank()) {
            val recommendations = AQI.getRecommendationsGivePm25(outDoorPm.toDouble())
            if (recommendations.size == 4) {
                binding.apply {
                    maskIv.setImageResource(recommendations[0].resourceId)
                    maskStatus.setText(recommendations[0].commentRes!!)
                    canGoOutIv.setImageResource(recommendations[1].resourceId)
                    canGoOutStatus.setText(recommendations[1].commentRes!!)
                    windowsIv.setImageResource(recommendations[2].resourceId)
                    windowsStatus.setText(recommendations[2].commentRes!!)
                    fanIv.setImageResource(recommendations[3].resourceId)
                    fanStatus.setText(recommendations[3].commentRes!!)
                    hasRecommendations = true
                }
            }
        }
        if (!hasRecommendations) {
            binding.apply {
                recommendationsTitle.isVisible = false
                line3.isVisible = false
                maskIv.isVisible = false
                maskStatus.isVisible = false
                canGoOutIv.isVisible = false
                canGoOutStatus.isVisible = false
                windowsIv.isVisible = false
                windowsStatus.isVisible = false
                fanIv.isVisible = false
                fanStatus.isVisible = false
            }
        }
    }

    private fun displayIndoorInfo(
        uiReadyPmInfo: MyLocationDetailsWrapper
    ) {
        val root = binding.pmInfoContainer
        val inOutLayoutView: View = root.findViewById(R.id.in_out_door_layout)
        inOutLayoutView.isVisible = true
        val inOutLayoutBinding = LocationDetailsInOutLayoutBinding.bind(inOutLayoutView)
        disableSliders(inOutLayoutBinding)
        inOutLayoutBinding.apply {
            indoorInfo.setBackgroundResource(uiReadyPmInfo.inBgTransparent)
            outdoorInfo.setBackgroundResource(uiReadyPmInfo.outBgTransparent)
            indoorPmValueTv.text = uiReadyPmInfo.inPmValueTxt
            outdoorPmValueTv.text = uiReadyPmInfo.outPmValueTxt
            indoorPmStatusIv.setImageResource(uiReadyPmInfo.inStatusIndicatorRes)
            outdoorPmStatusIv.setImageResource(uiReadyPmInfo.outStatusIndicatorRes)
            indoorPmStatusTv.text = uiReadyPmInfo.inStatusTvTxt
            outdoorPmStatusTv.text = uiReadyPmInfo.outStatusTvTxt
            updatedOnTv.text = uiReadyPmInfo.updatedOnTxt
            outdoorLocation.text = uiReadyPmInfo.locationArea
            indoorPmIndex.text = getString(R.string.default_pm_index_value)
            indoorPmIndexVal.text = uiReadyPmInfo.ogInPmTxt

            //pm values
            indoorPmIndexGradient.max = uiReadyPmInfo.pmSliderMax
            indoorPmIndexGradient.progress = uiReadyPmInfo.pmSliderValue
            indoorPmIndexGradient.thumb = ContextCompat.getDrawable(
                requireContext(),
                uiReadyPmInfo.pmSliderDiskRes
            )

            //co2
            if (uiReadyPmInfo.co2Slider != UNSET_PARAM_VAL) {
                indoorCoVal.text = uiReadyPmInfo.co2LvlTxt
                indoorCoGradient.max = uiReadyPmInfo.co2SliderMax
                indoorCoGradient.progress = uiReadyPmInfo.co2Slider
                indoorCoGradient.thumb = ContextCompat.getDrawable(
                    requireContext(),
                    uiReadyPmInfo.coSliderDiskRes
                )
            } else {
                indoorCoLbl.isVisible = false
                indoorCoVal.isVisible = false
                indoorCoGradient.isVisible = false
            }

            //tmp
            if (uiReadyPmInfo.tmpSlider != UNSET_PARAM_VAL) {
                indoorTmpValue.text = uiReadyPmInfo.tmpLvl
                indoorTmpGradient.max = uiReadyPmInfo.tmpSliderMax
                indoorTmpGradient.progress = uiReadyPmInfo.tmpSlider
                indoorTmpGradient.thumb = ContextCompat.getDrawable(
                    requireContext(),
                    uiReadyPmInfo.tmpSliderDiskRes
                )
            } else {
                indoorTmpLbl.isVisible = false
                indoorTmpValue.isVisible = false
                indoorTmpGradient.isVisible = false
            }

            //tvoc
            if (uiReadyPmInfo.vocSlider != UNSET_PARAM_VAL) {
                indoorTvocVal.text = uiReadyPmInfo.vocLvlTxt
                indoorTvocGradient.max = uiReadyPmInfo.vocSliderMax
                indoorTvocGradient.progress = uiReadyPmInfo.vocSlider
                indoorTvocGradient.thumb = ContextCompat.getDrawable(
                    requireContext(),
                    uiReadyPmInfo.vocSliderDiskRes
                )
            } else {
                indoorTvocLbl.isVisible = false
                indoorTvocVal.isVisible = false
                indoorTvocGradient.isVisible = false
            }

            //humidity
            if (uiReadyPmInfo.humidSlider != UNSET_PARAM_VAL) {
                indoorHumidityVal.text = uiReadyPmInfo.humidLvl
                indoorHumiditygradient.max = uiReadyPmInfo.humidSliderMax
                indoorHumiditygradient.progress = uiReadyPmInfo.humidSlider
                indoorHumiditygradient.thumb = ContextCompat.getDrawable(
                    requireContext(),
                    uiReadyPmInfo.humidSliderDiskRes
                )
            } else {
                indoorHumidityLbl.isVisible = false
                indoorHumidityVal.isVisible = false
                indoorHumiditygradient.isVisible = false
            }
            //energy savings
            if (uiReadyPmInfo.carbonSavedStr.isBlank() || uiReadyPmInfo.energySavedStr.isBlank()) {
                //hide related views
                indoorEnergyTitle.isVisible = false
                energySavedLbl.isVisible = false
                energyLastThirtyLbl.isVisible = false
                energySavedInfoContainer.isVisible = false
                carbonSavedInfoContainer.isVisible = false
                carbonSavedLbl.isVisible = false
                lastThirtyLbl.isVisible = false
            } else {
                energySavedValue.text = uiReadyPmInfo.energySavedStr
                carbonSavedValue.text = uiReadyPmInfo.carbonSavedStr
            }

        }
    }

    private fun displayOutDoorInfo(
        uiReadyInfo: MyLocationDetailsWrapper
    ) {
        val root = binding.pmInfoContainer
        val outLayoutView: View = root.findViewById(R.id.out_door_layout)
        outLayoutView.isVisible = true
        val outLayoutBinding = LocationDetailsOutLayoutBinding.bind(outLayoutView)
        outLayoutBinding.apply {
            pmIndex.text = uiReadyInfo.aqiIndex
            pointsTv.text = uiReadyInfo.outPmValueTxt
            statusIv.setImageResource(uiReadyInfo.outStatusIndicatorRes)
            statusTv.text = uiReadyInfo.outStatusTvTxt
            lastUpdateTv.text = uiReadyInfo.updatedOnTxt
            if (uiReadyInfo.outDoorPmTxtColor != UNSET_PARAM_VAL) {
                val txtColor = ContextCompat.getColor(
                    requireContext(),
                    uiReadyInfo.outDoorPmTxtColor
                )
                pointsTv.setTextColor(txtColor)
                statusTv.setTextColor(txtColor)
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}