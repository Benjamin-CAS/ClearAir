package com.android_dev.cleanairspaces.ui.adding_locations.add

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.ActivityAddLocationBinding
import com.android_dev.cleanairspaces.persistence.api.responses.LocationDataFromQr
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.ui.welcome.SplashActivity
import com.android_dev.cleanairspaces.utils.CasEncDecQrProcessor
import com.android_dev.cleanairspaces.utils.MonitorTypes.getDeviceInfoByType
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.myTxt
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AddLocationActivity : AppCompatActivity() {
    companion object {
        private val TAG = AddLocationActivity::class.java.simpleName
        const val INTENT_FROM_QR_SCANNER_TAG = "loc_data_from_qr"
        const val INTENT_FROM_SEARCHED_INDOOR_LOC = "loc_data_is_indoor_query"
        const val INTENT_FROM_SEARCHED_OUTDOOR_LOC = "loc_data_is_outdoor_query"
        const val INTENT_FROM_SEARCHED_MONITOR_LOC: String = "loc_data_is_monitor_query"
    }
    @Inject
    lateinit var myLogger: MyLogger

    private lateinit var binding: ActivityAddLocationBinding
    private val viewModel: AddLocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLocationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar.toolbar)
        binding.toolbar.toolbar.apply {
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                this@AddLocationActivity.finish()
            }
        }
        viewModel.clearCache()

        when {
            intent.hasExtra(INTENT_FROM_SEARCHED_INDOOR_LOC) -> {
                val searchDataForIndoorLocation = intent.getParcelableExtra<SearchSuggestionsData>(INTENT_FROM_SEARCHED_INDOOR_LOC)
                myLogger.logThis(
                        TAG, "onCreate()",
                        "receivedData for indoor loc"
                )
                displayLocationData(
                        searchDataForIndoorLocation = searchDataForIndoorLocation
                )
            }

            intent.hasExtra(INTENT_FROM_SEARCHED_MONITOR_LOC) -> {

                myLogger.logThis(
                        TAG, "onCreate()",
                        "receivedData for monitor loc"
                )
            }

            intent.hasExtra(INTENT_FROM_SEARCHED_OUTDOOR_LOC) -> {
                val searchDataForOutDoorLocation = intent.getParcelableExtra<SearchSuggestionsData>(INTENT_FROM_SEARCHED_OUTDOOR_LOC)
                myLogger.logThis(
                        TAG, "onCreate()",
                        "receivedData for outdoor loc"
                )
                displayLocationData(
                        searchDataForOutdoorLocation = searchDataForOutDoorLocation
                )
            }

            intent.hasExtra(INTENT_FROM_QR_SCANNER_TAG) -> {
                val qrContent = intent.getStringExtra(INTENT_FROM_QR_SCANNER_TAG)
                myLogger.logThis(
                        TAG, "onCreate()",
                        "receivedData from qr $qrContent"
                )
                handleQrCode(qrContent)
            }
        }

        viewModel.observeScanQrCodeData().observe(this, Observer {
            displayLocationData(locationDataFromQr = it)
        })

        viewModel.observeAddProcess().observe(this, Observer {

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
                        binding.apply {
                            addLocationBtn.apply {
                                setText(R.string.location_added_text)
                                isEnabled = false
                                setCompoundDrawablesWithIntrinsicBounds(
                                        null,
                                        null,
                                        ContextCompat.getDrawable(
                                                this@AddLocationActivity,
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
                myLogger.logThis(
                        TAG, "observeAddProcess()", "exception ${exc.message}", exc
                )
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
            var onClickRemoveLocListener = ""
            var isSecureLocation = false
            when {
                locationDataFromQr != null -> {
                    if (locationDataFromQr.monitor_id.isNotBlank() && !locationDataFromQr.type.isNullOrBlank()) {
                        val deviceInfo = getDeviceInfoByType(locationDataFromQr.type!!)
                        if (deviceInfo != null) {
                            binding.deviceLogo.isVisible = true
                            Glide.with(this@AddLocationActivity)
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

                    Glide.with(this@AddLocationActivity)
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
                        addLocationFromSearchedInfo(searchInfo = searchDataForIndoorLocation, isInDoorData = true)
                    }
                }
                searchDataForOutdoorLocation != null -> {
                    //an outdoor loc has location_id , monitor_id
                    val locationLbl = getString(R.string.location_lbl)
                    infoText += "$locationLbl: ${searchDataForOutdoorLocation.nameToDisplay}"
                    onClickAddLocListener = {
                        addLocationFromSearchedInfo(searchInfo = searchDataForOutdoorLocation, isInDoorData = false)
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
                    reloadApp()
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
            viewModel.saveWatchedLocationFromScannedQr(monitorDataFromQr = locationDataFromQr, userName = userName
                    ?: "", userPwd = password ?: "")
        } else if (locationDataFromQr.location_id.isNotBlank() && locationDataFromQr.company_id.isNotBlank()) {
            viewModel.saveWatchedLocationFromScannedQr(locationDataFromQr = locationDataFromQr, userName = userName
                    ?: "", userPwd = password ?: "")
        }
    }

    private fun addLocationFromSearchedInfo(searchInfo: SearchSuggestionsData, isInDoorData: Boolean) {
        //an outdoor loc has location_id , monitor_id
        if (!isInDoorData) {
            viewModel.saveWatchedOutdoorLocationSearchedInfo(outDoorInfo = searchInfo)
        } else {
            val (userName, password) = if (searchInfo.is_secure) {
                Pair(binding.userName.myTxt(binding.userName)
                        ?: "", binding.password.myTxt(binding.password) ?: "")
            } else Pair("", "")
            viewModel.saveWatchedIndoorLocationSearchedInfo(
                    userName, password, searchInfo
            )
        }
    }


    private fun reloadApp(){
        this@AddLocationActivity.finish()
    }


}