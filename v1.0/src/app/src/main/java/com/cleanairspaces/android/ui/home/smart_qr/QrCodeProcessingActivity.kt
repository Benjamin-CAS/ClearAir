package com.cleanairspaces.android.ui.home.smart_qr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityAmapBinding
import com.cleanairspaces.android.databinding.ActivityGmapBinding
import com.cleanairspaces.android.databinding.ActivityQrCodeProcessingBinding
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.QrCodeProcessor
import com.google.android.gms.dynamic.IFragmentWrapper
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
                    viewModel.addLocationFromCompanyInfo(locId = parsedResult.locId , compId = parsedResult.compId)
                }
            }else {
                progressCircular.isVisible = false
                info.setText(parsedResult.codeRes)
            }
        }
    }

}