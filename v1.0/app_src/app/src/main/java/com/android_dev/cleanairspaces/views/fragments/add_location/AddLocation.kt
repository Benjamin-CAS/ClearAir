package com.android_dev.cleanairspaces.views.fragments.add_location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentAddLocationBinding
import com.android_dev.cleanairspaces.persistence.api.responses.LocationDataFromQr
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.utils.CasEncDecQrProcessor
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MonitorTypes.getDeviceInfoByType
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.myTxt
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddLocation : Fragment() {

    companion object {
        private val TAG = AddLocation::class.java.simpleName
    }

    @Inject
    lateinit var myLogger: MyLogger

    private val viewModel: AddLocationViewModel by activityViewModels()
    private var _binding: FragmentAddLocationBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val args: AddLocationArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.clearCache()

        when {

            args.locDataIsIndoorQuery != null -> {

                val searchDataForIndoorLocation = args.locDataIsIndoorQuery
                displayLocationData(
                        searchDataForIndoorLocation = searchDataForIndoorLocation
                )
            }


            args.locDataIsOutdoorQuery != null -> {
                val searchDataForOutDoorLocation = args.locDataIsOutdoorQuery
                displayLocationData(
                        searchDataForOutdoorLocation = searchDataForOutDoorLocation
                )
            }

            args.locDataFromQr != null -> {
                val qrContent = args.locDataFromQr
                handleQrCode(qrContent)
            }
        }

        viewModel.observeScanQrCodeData().observe(viewLifecycleOwner, {
            displayLocationData(locationDataFromQr = it)
        })

        viewModel.observeAddProcess().observe(viewLifecycleOwner, {

            try {
                when (it) {
                    WatchLocationProcessState.ADDING -> {
                        binding.apply {
                            addLocationBtn.isEnabled = false
                            progressCircular.isVisible = true
                            addingProgress.setText(R.string.add_location_ongoing_text)
                        }
                    }
                    WatchLocationProcessState.ADDED -> {
                        viewModel.refreshRecentlyAddedLocationDetails()
                        binding.apply {
                            addLocationBtn.apply {
                                setText(R.string.location_added_text)
                                isEnabled = false
                                setCompoundDrawablesWithIntrinsicBounds(
                                        null,
                                        null,
                                        ContextCompat.getDrawable(
                                                requireContext(),
                                                R.drawable.ic_white_check
                                        ),
                                        null
                                )
                            }
                            addingProgress.setText(R.string.location_added_text)
                            progressCircular.isVisible = false
                            addingProgress.text = ""

                        }
                    }
                    WatchLocationProcessState.ADDED_INDOOR -> {
                        //navigate to another search like location
                        val action = AddLocationDirections.actionAddLocationToAddLocationFromLocationsList()
                        findNavController().navigate(action)
                        viewModel.resetWatchLocationState() //so user can navigate back
                    }
                    WatchLocationProcessState.FAILED -> {
                        binding.apply {
                            addLocationBtn.isEnabled = true
                            progressCircular.isVisible = false
                            addingProgress.setText(R.string.add_location_failed_text)
                        }
                    }
                    else -> {
                    }
                }

            } catch (exc: Exception) {
                lifecycleScope.launch(Dispatchers.IO) {
                    myLogger.logThis(tag = LogTags.EXCEPTION, from = "$TAG observeAddProcess()", msg = exc.message, exc = exc)
                }
            }
        })
    }


    private fun displayLocationData(
            locationDataFromQr: LocationDataFromQr? = null,
            searchDataForIndoorLocation: SearchSuggestionsData? = null,
            searchDataForOutdoorLocation: SearchSuggestionsData? = null
    ) {
        binding.apply {
            var infoText = ""
            var onClickAddLocListener = {}
            var isSecureLocation = false
            when {
                locationDataFromQr != null -> {
                    if (locationDataFromQr.monitor_id.isNotBlank() && !locationDataFromQr.type.isNullOrBlank()) {
                        val deviceInfo = getDeviceInfoByType(locationDataFromQr.type!!)
                        if (deviceInfo != null) {
                            binding.deviceLogo.isVisible = true
                            Glide.with(requireContext())
                                    .load(locationDataFromQr.getFullDeviceLogoUrl(deviceInfo.deviceLogoName))
                                    .into(binding.deviceLogo)
                            val deviceInfoTxt =
                                    getString(R.string.device_info_lbl) + "\n" + getString(deviceInfo.deviceTitleRes)
                            val deviceIdLbl = getString(R.string.device_id_lbl)
                            infoText += "$deviceInfoTxt\n$deviceIdLbl: ${locationDataFromQr.monitor_id}\n"

                        } else {
                            binding.deviceLogo.isVisible = false
                        }
                    }

                    Glide.with(requireContext())
                            .load(locationDataFromQr.getFullLogoUrl())
                            .into(logo)
                    val locationInfoTitle = getString(R.string.location_information)
                    val companyLblTxt = getString(R.string.company_name_lbl)
                    val locationLblTxt = getString(R.string.location_lbl)
                    infoText += "$locationInfoTitle\n$companyLblTxt: ${locationDataFromQr.company}\n$locationLblTxt: ${locationDataFromQr.location}"
                    isSecureLocation = locationDataFromQr.is_secure
                    onClickAddLocListener = {
                        addLocationFromScannedQrInfo(locationDataFromQr)
                    }
                }
                searchDataForIndoorLocation != null -> {
                    //an indoor loc has  only company id
                    val companyLblTxt = getString(R.string.company_name_lbl)
                    infoText += "$companyLblTxt: ${searchDataForIndoorLocation.nameToDisplay}"
                    isSecureLocation = searchDataForIndoorLocation.is_secure
                    onClickAddLocListener = {
                        addLocationFromSearchedInfo(
                                searchInfo = searchDataForIndoorLocation,
                                isInDoorData = true
                        )
                    }
                }
                searchDataForOutdoorLocation != null -> {
                    //an outdoor loc has location_id , monitor_id
                    val locationLbl = getString(R.string.location_lbl)
                    infoText += "$locationLbl: ${searchDataForOutdoorLocation.nameToDisplay}"
                    onClickAddLocListener = {
                        addLocationFromSearchedInfo(
                                searchInfo = searchDataForOutdoorLocation,
                                isInDoorData = false
                        )
                    }
                }
            }

            info.text = infoText
            addLocationBtn.isVisible = true
            addLocationBtn.setOnClickListener {
                onClickAddLocListener()
            }
            cancelBtn.apply {
                isVisible = true
                setOnClickListener {
                    findNavController().navigateUp()
                }
            }
            if (isSecureLocation) {
                userName.isVisible = true
                password.isVisible = true
            } else {
                userName.isVisible = false
                password.isVisible = false
            }
            progressCircular.isVisible = false
        }
    }


    private fun handleQrCode(scannedQrContent: String?) {
        val locationDataFromQrt = CasEncDecQrProcessor.identifyQrCode(scannedQrContent)
        binding.apply {
            val processingQrCodeTxt = getString(R.string.processing_qr_code)
            info.text = processingQrCodeTxt
            if (locationDataFromQrt.codeRes == 200) {
                val infoTxt = processingQrCodeTxt + "\n" + locationDataFromQrt.extraData
                info.text = infoTxt
                if (locationDataFromQrt.monitorId != null) {
                    val monitorId = locationDataFromQrt.monitorId
                    viewModel.fetchLocationDetailsForScannedMonitor(monitorId)
                } else if (locationDataFromQrt.locId != null && locationDataFromQrt.compId != null) {
                    val locId = locationDataFromQrt.locId
                    val compId = locationDataFromQrt.compId
                    viewModel.fetchLocationDetailsForScannedDeviceWithCompLoc(
                            locId = locId, compId = compId
                    )
                }
            } else {
                progressCircular.isVisible = false
                info.setText(locationDataFromQrt.codeRes)
            }
        }
    }

    private fun addLocationFromScannedQrInfo(locationDataFromQr: LocationDataFromQr) {
        val (userName, password) = if (locationDataFromQr.is_secure) {
            Pair(binding.userName.myTxt(binding.userName), binding.password.myTxt(binding.password))
        } else Pair("", "")
        if (locationDataFromQr.monitor_id.isNotBlank()) {
            viewModel.saveWatchedLocationFromScannedQr(
                    monitorDataFromQr = locationDataFromQr, userName = userName
                    ?: "", userPwd = password ?: ""
            )
        } else if (locationDataFromQr.location_id.isNotBlank() && locationDataFromQr.company_id.isNotBlank()) {
            viewModel.saveWatchedLocationFromScannedQr(
                    locationDataFromQr = locationDataFromQr, userName = userName
                    ?: "", userPwd = password ?: ""
            )
        }
    }

    private fun addLocationFromSearchedInfo(
            searchInfo: SearchSuggestionsData,
            isInDoorData: Boolean
    ) {
        //an outdoor loc has location_id , monitor_id
        if (!isInDoorData) {
            viewModel.saveWatchedOutdoorLocationSearchedInfo(outDoorInfo = searchInfo)
        } else {
            val (userName, password) = if (searchInfo.is_secure) {
                Pair(
                        binding.userName.myTxt(binding.userName)
                                ?: "", binding.password.myTxt(binding.password) ?: ""
                )
            } else Pair("", "")
            viewModel.saveWatchedIndoorLocationSearchedInfo(
                    userName, password, searchInfo
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetWatchLocationState() //so user can navigate back
        _binding = null
    }
}