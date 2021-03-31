package com.cleanairspaces.android.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (false) {
            //TODO CALL checkForGooglePlayServices()
           MyLogger.logThis(TAG, "onViewCreated()" , "google play services found & up to date")
            startActivity(Intent(this, GMapActivity::class.java))
       } else {
           MyLogger.logThis(TAG, "onViewCreated()" , "google play services not found & or out-dated")
            startActivity(Intent(this, AMapActivity::class.java))
       }

        this.finish()
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