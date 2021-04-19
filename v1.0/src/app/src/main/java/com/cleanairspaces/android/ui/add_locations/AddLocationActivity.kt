package com.cleanairspaces.android.ui.add_locations

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityAddLocationBinding
import com.cleanairspaces.android.models.entities.LocationDataFromQr
import com.cleanairspaces.android.models.entities.SearchSuggestions
import com.cleanairspaces.android.ui.BaseActivity
import com.cleanairspaces.android.utils.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddLocationActivity : BaseActivity() {
    companion object {
        private val TAG = AddLocationActivity::class.java.simpleName
        val INTENT_EXTRA_QR_CONTENT_TAG = "qrContent"
        val INTENT_EXTRA_SEARCHED_TAG = "searchedContent"
    }

    private lateinit var binding: ActivityAddLocationBinding

    private val viewModel: AddLocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLocationBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        //toolbar
        super.setToolBar(binding.toolbarLayout, isHomeAct = false)

        if (intent.hasExtra(INTENT_EXTRA_QR_CONTENT_TAG)) {
            //user has scanned a device qr code
            val scannedQrContent = intent.getStringExtra(INTENT_EXTRA_QR_CONTENT_TAG)
            handleQrCode(scannedQrContent)
        } else if (intent.hasExtra(INTENT_EXTRA_SEARCHED_TAG)) {
            //user has searched for a location
            val searchedItem = intent.getParcelableExtra<SearchSuggestions>(
                    INTENT_EXTRA_SEARCHED_TAG
            )
            searchedItem?.let {
                MyLogger.logThis(TAG, "search for ", "$it")
                when {
                    it.is_indoor_location -> {
                        observeLocationToAdd(compId = it.company_id.toInt())
                    }
                    it.company_id.isNotEmpty() && it.location_id.isNotEmpty() -> {
                        observeLocationToAdd(compId = it.company_id.toInt(), locId = it.location_id.toInt())
                    }
                    it.monitor_id.isNotEmpty() -> observeLocationToAdd(monitorId = it.monitor_id)
                }
            }
        }


        observeMyLocationAdd()
    }

    override fun handleBackPress() {
        this@AddLocationActivity.finish()
    }

    private fun observeMyLocationAdd() {
        viewModel.observeMyLocationOperation().observe(this, Observer { isSuccessful ->
            if (!isSuccessful) {
                binding.progressCircular.isVisible = false
                binding.container.showSnackBar(
                        msgResId = R.string.failed_to_add_location_check_credentials,
                        isErrorMsg = true,
                        actionMessage = R.string.dismiss,
                        actionToTake = {}
                )
            }
        })
    }

    private fun handleQrCode(scannedQrContent: String?) {
        val processingQrCodeTxt = getString(R.string.processing_qr_code)
        binding.info.text = processingQrCodeTxt
        val parsedResult = QrCodeProcessor.identifyQrCode(scannedQrContent)
        binding.apply {
            if (parsedResult.codeRes == 200) {
                val infoTxt = processingQrCodeTxt + "\n" + parsedResult.extraData
                info.text = infoTxt
                if (parsedResult.monitorId != null) {
                    observeLocationToAdd(
                            monitorId = parsedResult.monitorId
                    )
                    viewModel.addLocationFromMonitorId(monitorId = parsedResult.monitorId)
                } else if (parsedResult.locId != null && parsedResult.compId != null) {
                    observeLocationToAdd(
                            locId = parsedResult.locId,
                            compId = parsedResult.compId
                    )
                    viewModel.addLocationFromCompanyInfo(
                            locId = parsedResult.locId,
                            compId = parsedResult.compId
                    )
                }
            } else {
                progressCircular.isVisible = false
                info.setText(parsedResult.codeRes)
            }
        }
    }

    private fun observeLocationToAdd(
            locId: Int? = null,
            compId: Int? = null,
            monitorId: String? = null
    ) {
        when {
            compId != null && locId != null -> {
                //out door location
                viewModel.observeLocationFromCompanyInfo(locId = locId, compId = compId).observe(
                        this, Observer {
                    if (it != null) {
                        displayLocationInfo(it)

                    }
                }
                )
            }
            monitorId != null -> {
                //scanned monitor QR
                viewModel.observeLocationFromMonitorInfo(monitorId = monitorId).observe(
                        this, Observer {
                    if (it != null) {
                        displayLocationInfo(it)

                    }
                }
                )
            }
            compId != null -> {
                //indoor location
                viewModel.observeIndoorLocationFromCompInfo(compId = compId).observe(
                        this, Observer {
                    if (it != null) {
                        displayLocationInfo(it)
                    }
                }
                )
            }
        }
    }

    private fun displayLocationInfo(locationDataFromQr: LocationDataFromQr) {
        binding.apply {
            progressCircular.isVisible = false

            var infoText = ""

            if (!locationDataFromQr.monitor_id.isNullOrBlank() && !locationDataFromQr.type.isNullOrBlank()) {
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
            info.text = infoText
            addLocationBtn.isVisible = true
            cancelBtn.apply {
                isVisible = true
                setOnClickListener {
                    this@AddLocationActivity.finish()
                }
            }

            if (locationDataFromQr.is_mine) {
                addLocationBtn.apply {
                    setText(R.string.location_added_text)
                    isEnabled = false
                    setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            ContextCompat.getDrawable(
                                    this@AddLocationActivity,
                                    R.drawable.ic_on_secondary_check
                            ),
                            null
                    )
                }
                removeLocationBtn.apply {
                    isVisible = true
                    setOnClickListener {
                        toggleLocationIsMine(locationDataFromQr, isMine = false)
                    }
                }
            } else {
                addLocationBtn.apply {
                    setText(R.string.add_location_text)
                    isEnabled = true
                    setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    setOnClickListener {
                        toggleLocationIsMine(locationDataFromQr, isMine = true)
                    }
                }
                removeLocationBtn.isVisible = false
            }
            if (locationDataFromQr.is_secure) {
                userName.isVisible = true
                password.isVisible = true
            } else {
                userName.isVisible = false
                password.isVisible = false
            }
        }
    }

    private fun toggleLocationIsMine(locationDataFromQr: LocationDataFromQr, isMine: Boolean) {
        if (locationDataFromQr.is_secure && isMine) {
            //user is adding location
            val userName = binding.userName.myTxt(binding.userName)
            val userPassword = binding.password.myTxt(binding.password)
            if (userName.isNullOrBlank() || userPassword.isNullOrBlank()) {
                binding.container.showSnackBar(
                        msgResId = R.string.enter_username_password,
                        isErrorMsg = true
                )
            } else {
                binding.progressCircular.isVisible = true
                viewModel.toggleLocationIsMine(
                        locationDataFromQr,
                        userName,
                        userPassword,
                        isMine = true
                )
            }
        } else {
            binding.progressCircular.isVisible = true
            viewModel.toggleLocationIsMine(locationDataFromQr, isMine = isMine)
        }
    }

}