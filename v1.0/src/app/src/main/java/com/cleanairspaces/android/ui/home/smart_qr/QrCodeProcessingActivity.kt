package com.cleanairspaces.android.ui.home.smart_qr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityQrCodeProcessingBinding
import com.cleanairspaces.android.utils.QrCodeProcessor
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
                    viewModel.addLocationFromMonitorId(monitorId = parsedResult.monitorId)
                }
                else if (parsedResult.locId != null && parsedResult.compId != null){
                    observeThisLocation(
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

    private fun observeThisLocation(locId: Int, compId: Int) {
        viewModel.observeLocationFromCompanyInfo(locId = locId, compId = compId).observe(
            this, Observer {
                if (it != null){
                    binding.apply {
                        progressCircular.isVisible = false
                        val refMonLbl = getString(R.string.ref_mon_lbl)
                        val companyLblTxt = getString(R.string.company_name_lbl)
                        val locationLblTxt = getString(R.string.location_lbl)
                        val infoText = "$companyLblTxt\n${it.company}\n$locationLblTxt:${it.location}\n${it.dev_name}$refMonLbl:${it.reference_mon}"
                        info.text = infoText
                        addLocationBtn.isVisible = true
                        cancelBtn.isVisible = true
                    }
                }
            }
        )
    }

}