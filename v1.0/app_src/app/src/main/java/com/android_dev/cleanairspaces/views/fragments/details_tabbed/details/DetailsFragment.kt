package com.android_dev.cleanairspaces.views.fragments.details_tabbed.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentDetailsBinding
import com.android_dev.cleanairspaces.databinding.LocationDetailsInOutLayoutBinding
import com.android_dev.cleanairspaces.databinding.LocationDetailsOutLayoutBinding
import com.android_dev.cleanairspaces.databinding.RecommendationsLayoutBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.InOutPmFormattedOverviewData
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.IndoorFormatterExtraDetailsData
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.formatWatchedHighLightsData
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.formatWatchedHighLightsIndoorExtras
import com.android_dev.cleanairspaces.utils.AQIStatus
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.getRecommendationsGivenAQIColorRes
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    companion object {
        private val TAG = DetailsFragment::class.java.simpleName
    }

    @Inject
    lateinit var myLogger: MyLogger

    private lateinit var recommendationsViewBinding: RecommendationsLayoutBinding
    private lateinit var recommendationsView: View
    private lateinit var outLayoutBinding: LocationDetailsOutLayoutBinding
    private lateinit var inOutLayoutBinding: LocationDetailsInOutLayoutBinding
    private lateinit var inOutLayoutView: View
    private lateinit var outOnlyLayoutView: View

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailsViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        val root = binding.pmInfoContainer
        inOutLayoutView = root.findViewById(R.id.in_out_door_layout)
        inOutLayoutBinding = LocationDetailsInOutLayoutBinding.bind(inOutLayoutView)
        disableSliders(inOutLayoutBinding)
        outOnlyLayoutView = root.findViewById(R.id.out_door_layout)
        outLayoutBinding = LocationDetailsOutLayoutBinding.bind(outOnlyLayoutView)

        recommendationsView = root.findViewById(R.id.recommendations_layout_id)
        recommendationsViewBinding = RecommendationsLayoutBinding.bind(recommendationsView)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeWatchedLocationWithAqi().observe(viewLifecycleOwner, {
            it?.let { watchedLocationWithAqi ->
                updateGenDetails(watchedLocationWithAqi.watchedLocationHighLights)
                refreshWatchedLocationDetails(watchedLocationWithAqi.watchedLocationHighLights, watchedLocationWithAqi.aqiIndex)
            }
        })


    }

    private fun updateGenDetails(watchedLocation: WatchedLocationHighLights) {
        val logoURL = watchedLocation.getFullLogoUrl()
        lifecycleScope.launch(Dispatchers.IO) {
            myLogger.logThis(
                    tag = LogTags.USER_ACTION_OPEN_SCREEN,
                    from = TAG,
                    msg = "viewing ${watchedLocation.name}"
            )
        }
        binding.apply {
            if (logoURL.isNotBlank()) {
                locationLogo.isVisible = true
                Glide.with(requireContext())
                        .load(logoURL)
                        .into(locationLogo)
            }
            locationNameTv.text = if (watchedLocation.location_area.isNotBlank())
                watchedLocation.location_area
            else watchedLocation.name
            locationNameTv.isSelected = true
        }
    }

    private fun refreshWatchedLocationDetails(watchedLocation: WatchedLocationHighLights, aqiIndex : String?) {
        binding.progressCircular.isVisible = true
        val inOutData = formatWatchedHighLightsData(
                ctx = requireContext(),
                location = watchedLocation,
                aqiIndex = aqiIndex
        )
        binding.aqiIndex.text = inOutData.aqiIndexStr
        if (inOutData.hasInDoorData) {
            outOnlyLayoutView.isVisible = false
            inOutLayoutView.isVisible = true
            displayInOutInfo(inOutData)
            if (inOutData.indoorPmValue != null && inOutData.indoorAQIStatus != null)
                displayIndoorDetailsSection(
                        indoorAQIStatus = inOutData.indoorAQIStatus,
                        indoorHumidity = watchedLocation.indoor_humidity,
                        indoorCo2 = watchedLocation.indoor_co2,
                        indoorVoc = watchedLocation.indoor_voc,
                        indoorTemperature = watchedLocation.indoor_temperature,
                        energyMax = watchedLocation.energyMax,
                        energyMonth = watchedLocation.energyMonth
                )
        } else {
            inOutLayoutView.isVisible = false
            if (inOutData.hasOutDoorData) {
                outOnlyLayoutView.isVisible = true
                displayOutDoorInfo(inOutData)
            }
        }
        if (inOutData.hasOutDoorData) {
            displayRecommendations(inOutData.outDoorAqiStatus)
        }

        binding.progressCircular.isVisible = false
    }

    private fun displayIndoorDetailsSection(
            indoorAQIStatus: AQIStatus,
            indoorHumidity: Double?,
            indoorCo2: Double?,
            indoorVoc: Double?,
            indoorTemperature: Double?,
            energyMax: Double?,
            energyMonth: Double?
    ) {
        val uiReadyPmInfo = formatWatchedHighLightsIndoorExtras(
                ctx = requireContext(),
                inDoorAqiStatus = indoorAQIStatus,
                humidLvl = indoorHumidity,
                co2Lvl = indoorCo2,
                vocLvl = indoorVoc,
                tmpLvl = indoorTemperature,
                energyMax = energyMax,
                energyMonth = energyMonth
        )

        inOutLayoutBinding.apply {
            indoorPmIndexGradient.max = IndoorFormatterExtraDetailsData.sliderMaxFor3GradientLvls
            indoorPmIndexGradient.progress = uiReadyPmInfo.pmSliderValue
            indoorPmIndexGradient.thumb = ContextCompat.getDrawable(
                    requireContext(),
                    indoorAQIStatus.diskRes
            )

            //co2
            if (uiReadyPmInfo.co2SliderValue != null && uiReadyPmInfo.coSliderDiskRes != null) {
                indoorCo2Val.text = uiReadyPmInfo.co2LvlTxt
                indoorCo2Gradient.max = IndoorFormatterExtraDetailsData.sliderMaxFor3GradientLvls
                indoorCo2Gradient.progress = uiReadyPmInfo.co2SliderValue
                indoorCo2Gradient.thumb = ContextCompat.getDrawable(
                        requireContext(),
                        uiReadyPmInfo.coSliderDiskRes
                )
            } else {
                indoorCo2Lbl.isVisible = false
                indoorCo2Val.isVisible = false
                indoorCo2Gradient.isVisible = false
            }

            //tmp
            if (uiReadyPmInfo.tmpSliderValue != null &&
                    uiReadyPmInfo.tmpSliderDiskRes != null
            ) {
                indoorTmpValue.text = uiReadyPmInfo.tmpLvlTxt
                indoorTmpGradient.max = IndoorFormatterExtraDetailsData.sliderMaxForSixGradientLvls
                indoorTmpGradient.progress = uiReadyPmInfo.tmpSliderValue
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
            if (uiReadyPmInfo.vocSliderValue != null && uiReadyPmInfo.vocSliderDiskRes != null) {
                indoorTvocVal.text = uiReadyPmInfo.vocLvlTxt
                indoorTvocGradient.max = IndoorFormatterExtraDetailsData.sliderMaxFor3GradientLvls
                indoorTvocGradient.progress = uiReadyPmInfo.vocSliderValue
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
            if (uiReadyPmInfo.humidSliderValue != null && uiReadyPmInfo.humidSliderDiskRes != null) {
                indoorHumidityVal.text = uiReadyPmInfo.humidLvlTxt
                indoorHumiditygradient.max =
                        IndoorFormatterExtraDetailsData.sliderMaxForSixGradientLvls
                indoorHumiditygradient.progress = uiReadyPmInfo.humidSliderValue
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

    private fun displayRecommendations(outDoorAqiStatus: AQIStatus?) {
        recommendationsView.isVisible = (outDoorAqiStatus != null)
        outDoorAqiStatus?.let {
            val recommendations = getRecommendationsGivenAQIColorRes(it.aqi_color_res)
            if (recommendations.size == 4) {
                recommendationsViewBinding.apply {
                    maskIv.setImageResource(recommendations[0].resourceId)
                    maskStatus.setText(recommendations[0].commentRes!!)
                    canGoOutIv.setImageResource(recommendations[1].resourceId)
                    canGoOutStatus.setText(recommendations[1].commentRes!!)
                    windowsIv.setImageResource(recommendations[2].resourceId)
                    windowsStatus.setText(recommendations[2].commentRes!!)
                    fanIv.setImageResource(recommendations[3].resourceId)
                    fanStatus.setText(recommendations[3].commentRes!!)
                }
            }
        }
    }


    private fun displayInOutInfo(inOutData: InOutPmFormattedOverviewData) {
        inOutData.indoorAQIStatus?.let { inAqiStatus ->
            inOutLayoutBinding.apply {
                indoorInfo.setBackgroundResource(inAqiStatus.transparentRes)
                indoorPmValueTv.text = inOutData.indoorPmValueConverted.toString()
                indoorPmValueTv.setTextColor(ContextCompat.getColor(requireContext(), inAqiStatus.txtColorRes))
                indoorPmStatusLine.setImageResource(inAqiStatus.status_bar_res)
                indoorPmStatusTv.setText(inAqiStatus.lbl)
                updatedOnTv.text = inOutData.updated
                indoorPmIndex25Lbl.text = getString(R.string.default_aqi_pm_2_5)
                val pmValTxt = inOutData.indoorPmValue.toString() + " " + getString(R.string.pm_units)
                indoorPmIndexVal.text = pmValTxt
            }
        }
        inOutData.outDoorAqiStatus?.let { outAqiStatus ->
            inOutLayoutBinding.apply {
                outdoorInfo.setBackgroundResource(outAqiStatus.transparentRes)
                outdoorPmStatusLine.setImageResource(outAqiStatus.status_bar_res)
                outdoorPmStatusTv.setText(outAqiStatus.lbl)
                outdoorPmValueTv.text = inOutData.outDoorPmValue.toString()
                outdoorPmValueTv.setTextColor(ContextCompat.getColor(requireContext(), outAqiStatus.txtColorRes))
                outdoorLocation.text = inOutData.locationName
                outdoorLocation.isSelected = true
            }
        }
    }

    private fun disableSliders(inOutLayoutBinding: LocationDetailsInOutLayoutBinding) {
        inOutLayoutBinding.apply {
            indoorPmIndexGradient.isEnabled = false
            indoorCo2Gradient.isEnabled = false
            indoorTmpGradient.isEnabled = false
            indoorTvocGradient.isEnabled = false
        }
    }

    private fun displayOutDoorInfo(uiReadyInfo: InOutPmFormattedOverviewData) {
        outLayoutBinding.apply {
            outPmIndexValue.text = uiReadyInfo.outDoorPmValue.toString()
            uiReadyInfo.outDoorAqiStatus?.let { aqiStatus ->
                outPmStatusLine.setImageResource(aqiStatus.status_bar_res)
                statusTv.setText(aqiStatus.lbl)
                val aqiColor = ContextCompat.getColor(
                        requireContext(),
                        aqiStatus.aqi_color_res
                )
                outPmIndexValue.setTextColor(aqiColor)
                statusTv.setTextColor(aqiColor)
            }
            lastUpdateTv.text = uiReadyInfo.updated
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}