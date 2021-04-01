package com.cleanairspaces.android.ui.home.smart_qr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityAmapBinding
import com.cleanairspaces.android.databinding.ActivityGmapBinding
import com.cleanairspaces.android.databinding.ActivityQrCodeProcessingBinding
import com.cleanairspaces.android.utils.MyLogger

/*
**
//example monitor qrContent == http://monitor.cleanairspaces.com/downloadApp?LOCIDBHBXGEGYCEIZ
//or device qrContent ==  msg : qrContent NATDF483FDA0DBCB000000
 */
class QrCodeProcessingActivity : AppCompatActivity() {
    companion object{
        private val TAG = QrCodeProcessingActivity::class.java.simpleName
        val INTENT_EXTRA_TAG = "qrContent"
    }

    private lateinit var binding : ActivityQrCodeProcessingBinding
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
        MyLogger.logThis( TAG, "onCreate Received Intent--",
            "qrContent $scannedQrContent")
        binding.info.setText(R.string.processing_qr_code)

    }
}