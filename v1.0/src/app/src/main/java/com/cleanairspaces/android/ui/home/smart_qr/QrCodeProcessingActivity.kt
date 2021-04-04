package com.cleanairspaces.android.ui.home.smart_qr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityQrCodeProcessingBinding
import com.cleanairspaces.android.models.entities.CustomerDeviceData
import com.cleanairspaces.android.utils.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class QrCodeProcessingActivity : AppCompatActivity() {
    companion object{
        private val TAG = QrCodeProcessingActivity::class.java.simpleName
        val INTENT_EXTRA_TAG = "qrContent"
    }

    private lateinit var binding : ActivityQrCodeProcessingBinding

    private val viewModel : QrCodeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeProcessingBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        //toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.apply {
            Glide.with(this@QrCodeProcessingActivity)
                .load(R.drawable.clean_air_spaces_logo_name)
                .into(toolbarLogo)
            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener(
                View.OnClickListener {
                    this@QrCodeProcessingActivity.finish()
                }
            )
        }

        val scannedQrContent =  intent.getStringExtra(INTENT_EXTRA_TAG)
       handleQrCode(scannedQrContent)
    }

    private fun handleQrCode(scannedQrContent: String?) {
        val processingQrCodeTxt = getString(R.string.processing_qr_code)
        binding.info.text = processingQrCodeTxt
        val parsedResult =  QrCodeProcessor.identifyQrCode(scannedQrContent)
        binding.apply {
            if (parsedResult.codeRes == 200) {
                val infoTxt = processingQrCodeTxt + "\n" + parsedResult.extraData
                info.text = infoTxt
                if (parsedResult.monitorId != null){
                    observeScannedLocation(
                            monitorId =  parsedResult.monitorId
                    )
                    viewModel.addLocationFromMonitorId(monitorId = parsedResult.monitorId)
                }
                else if (parsedResult.locId != null && parsedResult.compId != null){
                    observeScannedLocation(
                        locId = parsedResult.locId,
                        compId = parsedResult.compId
                    )
                    viewModel.addLocationFromCompanyInfo(locId = parsedResult.locId , compId = parsedResult.compId)
                }
            }else {
                progressCircular.isVisible = false
                info.setText(parsedResult.codeRes)
            }
        }
    }

    private fun observeScannedLocation(locId: Int? = null, compId: Int? = null, monitorId : String? = null) {
        if (compId != null && locId != null) {
            viewModel.observeLocationFromCompanyInfo(locId = locId, compId = compId).observe(
                    this, Observer {
                if (it != null) {
                    displayLocationInfo(it)

                }
            }
            )
        }
        if (monitorId != null){
            viewModel.observeLocationFromMonitorInfo(monitorId = monitorId).observe(
                    this, Observer {
                if (it != null) {
                    displayLocationInfo(it)

                }
            }
            )
        }
    }

  private fun displayLocationInfo(customerDeviceData: CustomerDeviceData) {
      binding.apply {
          progressCircular.isVisible = false

          var infoText = ""

          if(!customerDeviceData.monitor_id.isNullOrBlank() && !customerDeviceData.type.isNullOrBlank()){
            val deviceInfo = getDeviceInfoByType(customerDeviceData.type!!)
            if (deviceInfo != null){
                binding.deviceLogo.isVisible = true
                Glide.with(this@QrCodeProcessingActivity)
                        .load(customerDeviceData.getFullDeviceLogoUrl(deviceInfo.deviceLogoName))
                        .into(binding.deviceLogo)
                val deviceInfoTxt = getString(R.string.device_info_lbl) + "\n" + getString(deviceInfo.deviceTitleRes)
                val deviceIdLbl = getString(R.string.device_id_lbl)
                infoText += "$deviceInfoTxt\n$deviceIdLbl: ${customerDeviceData.monitor_id}\n"

            }else{
                binding.deviceLogo.isVisible = false
            }
          }

          Glide.with(this@QrCodeProcessingActivity)
                  .load(customerDeviceData.getFullLogoUrl())
                  .into(logo)
          MyLogger.logThis(TAG, "displaying", customerDeviceData.getFullLogoUrl())
          val locationInfoTitle = getString(R.string.location_information)
          val companyLblTxt = getString(R.string.company_name_lbl)
          val locationLblTxt = getString(R.string.location_lbl)
          infoText += "$locationInfoTitle\n$companyLblTxt: ${customerDeviceData.company}\n$locationLblTxt: ${customerDeviceData.location}"
          info.text = infoText
          addLocationBtn.isVisible = true
          cancelBtn.isVisible = true

          if (customerDeviceData.isMyDeviceData) {
              addLocationBtn.apply {
                  setText(R.string.location_added_text)
                  isEnabled = false
                  setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this@QrCodeProcessingActivity, R.drawable.ic_on_secondary_check), null)
              }
          }else {
              addLocationBtn.apply {
                  setText(R.string.add_location_text)
                  isEnabled = true
                  setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                  setOnClickListener {
                      addLocationAsMine(customerDeviceData)
                  }
              }
          }
          if(customerDeviceData.isSecure){
              userName.isVisible = true
              password.isVisible = true
          }else{
              userName.isVisible = false
              password.isVisible = false
          }
      }
  }

    private fun addLocationAsMine(customerDeviceData: CustomerDeviceData){
        if (customerDeviceData.isSecure){
          val userName = binding.userName.myTxt(binding.userName)
          val userPassword = binding.password.myTxt(binding.password)
         if (userName.isNullOrBlank() || userPassword.isNullOrBlank()){
             binding.container.showSnackBar(
                     msgResId = R.string.enter_username_password,
                     isErrorMsg = true
             )
         }else {
             binding.progressCircular.isVisible = true
            viewModel.saveMyLocation(customerDeviceData, userName, userPassword)
         }
        }else{
            binding.progressCircular.isVisible = true
            viewModel.saveMyLocation(customerDeviceData)
        }
    }

}