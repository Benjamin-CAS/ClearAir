package com.cleanairspaces.android.ui.details

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityLocationDetailsBinding
import com.cleanairspaces.android.ui.BaseActivity
import com.cleanairspaces.android.utils.MyLocationDetailsWrapper
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class LocationDetailsActivity : BaseActivity() {
    companion object {
        private val TAG = LocationDetailsActivity::class.java.simpleName
        const val INTENT_EXTRA_TAG = "locationDetails"
    }


    private lateinit var binding: ActivityLocationDetailsBinding

    private val viewModel: LocationDetailsViewModel by viewModels()


    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationDetailsBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)


        //toolbar
        super.setToolBar(binding.toolbarLayout, isHomeAct = false)
        val locationDetailsInfo =
            intent.getParcelableExtra<MyLocationDetailsWrapper>(INTENT_EXTRA_TAG)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        //no home fragments
        appBarConfiguration = AppBarConfiguration(
            setOf()
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        locationDetailsInfo?.let { info ->
         val deviceId =  viewModel.setCustomerDeviceDataDetailedNGetDeviceId(myLocationDetailsWrapper = info)
         observeHistories(deviceId)
        }

    }

    override fun handleBackPress() {
        this@LocationDetailsActivity.finish()
    }


    private fun observeHistories(deviceId: String) {
        viewModel.observeHistories(deviceId).days.observe(this, androidx.lifecycle.Observer {
            if (it != null)
            viewModel.setDaysHistory(it)
        })
        viewModel.observeHistories(deviceId).week.observe(this, androidx.lifecycle.Observer {
            if (it != null)
            viewModel.setWeekHistory(it)
        })

        viewModel.observeHistories(deviceId).month.observe(this, androidx.lifecycle.Observer {
            if (it != null)
            viewModel.setMonthHistory(it)
        })

    }

    //toolbar handle back navigation
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}