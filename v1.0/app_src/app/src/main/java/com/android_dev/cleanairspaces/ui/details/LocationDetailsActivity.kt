package com.android_dev.cleanairspaces.ui.details

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.ActivityLocationDetailsBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.MyLogger
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationDetailsActivity : AppCompatActivity() {
    companion object {
        private val TAG = LocationDetailsActivity::class.java.simpleName
        const val INTENT_EXTRA_TAG = "locationDetails"
    }

    @Inject
    lateinit var myLogger: MyLogger


    private lateinit var binding: ActivityLocationDetailsBinding

    private val viewModel: LocationDetailsViewModel by viewModels()


    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationDetailsBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbarBarLayout.toolbar)
        binding.toolbarBarLayout.toolbar.apply {
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                this@LocationDetailsActivity.finish()
            }
        }


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
            }
        }

    }


    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

}