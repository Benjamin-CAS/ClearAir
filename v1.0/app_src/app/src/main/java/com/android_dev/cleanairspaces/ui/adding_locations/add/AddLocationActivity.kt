package com.android_dev.cleanairspaces.ui.adding_locations.add

import android.os.Bundle
import com.android_dev.cleanairspaces.BaseActivity
import com.android_dev.cleanairspaces.databinding.ActivityAddLocationBinding
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddLocationActivity : BaseActivity() {
    companion object {
        private val TAG = AddLocationActivity::class.java.simpleName
        const val INTENT_FROM_QR_SCANNER_TAG = "loc_data_from_qr"
        const val INTENT_FROM_SEARCHED_INDOOR_LOC = "loc_data_is_indoor_query"
        const val INTENT_FROM_SEARCHED_OUTDOOR_LOC = "loc_data_is_outdoor_query"
        const val INTENT_FROM_SEARCHED_MONITOR_LOC: String = "loc_data_is_monitor_query"
    }

    private lateinit var binding: ActivityAddLocationBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLocationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        super.setToolBar(binding.toolbar, isHomeAct = false)

        when {
            intent.hasExtra(INTENT_FROM_SEARCHED_INDOOR_LOC) -> {
                MyLogger.logThis(
                    TAG, "onCreate()",
                    "receivedData for indoor loc"
                )
            }

            intent.hasExtra(INTENT_FROM_SEARCHED_OUTDOOR_LOC) -> {
                MyLogger.logThis(
                    TAG, "onCreate()",
                    "receivedData for indoor loc"
                )
            }

            intent.hasExtra(INTENT_FROM_QR_SCANNER_TAG) -> {
                MyLogger.logThis(
                    TAG, "onCreate()",
                    "receivedData from qr ${intent.getStringExtra(INTENT_FROM_QR_SCANNER_TAG)}"
                )
            }
        }
    }

    override fun handleBackPress() {
        super.handleBackPress()
        finish()
    }

}