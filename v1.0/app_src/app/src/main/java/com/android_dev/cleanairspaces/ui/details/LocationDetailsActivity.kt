package com.android_dev.cleanairspaces.ui.details

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.android_dev.cleanairspaces.BaseActivity
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.ActivityLocationDetailsBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.bumptech.glide.Glide
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationDetailsBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        //toolbar
        super.setToolBar(binding.toolbarBarLayout, isHomeAct = false)


        //intent data
        val locationDetailsInfo =
            intent.getParcelableExtra<WatchedLocationHighLights>(INTENT_EXTRA_TAG)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNav.setupWithNavController(navController)

        locationDetailsInfo?.let {
            val logoURL = it.getFullLogoUrl()
            binding.apply {
                if (logoURL.isNotBlank()) {
                    locationLogo.isVisible = true
                    Glide.with(this@LocationDetailsActivity)
                        .load(logoURL)
                        .into(locationLogo)
                }
                locationNameTv.text = it.name
                viewModel.setWatchedLocation(it)
                observeHistories()
            }
        }

    }
    private fun observeHistories() {
        viewModel.observeHistories().days.observe(this, androidx.lifecycle.Observer {
            if (it != null)
                viewModel.setDaysHistory(it)
        })
        viewModel.observeHistories().week.observe(this, androidx.lifecycle.Observer {
            if (it != null)
                viewModel.setWeekHistory(it)
        })

        viewModel.observeHistories().month.observe(this, androidx.lifecycle.Observer {
            if (it != null)
                viewModel.setMonthHistory(it)
        })

    }

    override fun handleBackPress() {
        this@LocationDetailsActivity.finish()
    }


    //toolbar handle back navigation
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}