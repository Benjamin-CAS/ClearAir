package com.cleanairspaces.android.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityMainBinding
import com.cleanairspaces.android.ui.home.amap.AMapActivity
import com.cleanairspaces.android.ui.home.gmap.GMapActivity
import com.cleanairspaces.android.utils.MyLogger
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val TAG = MainActivity::class.java.simpleName

    private val viewModel: MainActViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel.refreshData()

        viewModel.dataRefresherWorkerInfo.observe(this, Observer {
            for (work in it) {
                MyLogger.logThis(
                    TAG, " onCreate() dataRefresherWorkerInfo() observe",
                    "work is in ${work.state} state"
                )
            }
        })

        val gMapTxt = getString(R.string.g_map_preferred)
        viewModel.getSelectedMap().observe(this, Observer {
            if (it.equals(gMapTxt)) {
                val hasGooglePlay = checkForGooglePlayServices()
                MyLogger.logThis(
                    TAG,
                    "onViewCreated()",
                    "google play services found & up to date == hasGooglePlay"
                )
                //todo if false and user has selected this. notify them first
                startActivity(Intent(this, GMapActivity::class.java))
            } else {
                startActivity(Intent(this, AMapActivity::class.java))
            }
            this.finish()
        })

    }

    private fun checkForGooglePlayServices(): Boolean {
        //maybe need update?
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        return when (googleApiAvailability.isGooglePlayServicesAvailable(
            this
        )) {
            ConnectionResult.SUCCESS -> true
            else -> false
        }
    }

}