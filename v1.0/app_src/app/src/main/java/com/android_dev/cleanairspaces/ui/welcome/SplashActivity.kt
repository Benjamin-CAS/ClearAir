package com.android_dev.cleanairspaces.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.android_dev.cleanairspaces.BaseActivity
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.ActivitySplashBinding
import com.android_dev.cleanairspaces.ui.home.maps.AMapsActivity
import com.android_dev.cleanairspaces.ui.home.maps.GMapsActivity
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.showSnackBar
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashActViewModel by viewModels()
    private val TAG = SplashActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        viewModel.initDataRefresh()
        viewModel.getMapSelected().observe(
            this, Observer {
                if (it != null && it != getString(R.string.default_map_a_map)) {
                    MyLogger.logThis(TAG, "getMapSelected()", "google maps selected")
                    if (checkForGooglePlayServices()) {
                        //send to google play services
                        navigateToActivity(GMapsActivity::class.java)
                    } else {
                        MyLogger.logThis(
                            TAG,
                            "getMapSelected()",
                            "google maps selected but google play services missing or out of data"
                        )
                        snackBar = binding.container.showSnackBar(
                            isErrorMsg = true,
                            msgResId = R.string.no_google_play_services_err,
                            actionMessage = R.string.switch_to_a_maps,
                            actionToTake = {
                                //send to a-maps activity
                                navigateToActivity(AMapsActivity::class.java)
                            }
                        )
                    }
                } else {
                    //use A MAP
                    MyLogger.logThis(TAG, "getMapSelected()", "a maps selected")
                    navigateToActivity(AMapsActivity::class.java)
                }
            }
        )

    }

    private fun navigateToActivity(toAct: Class<*>) {
        try {
            startActivity(Intent(this, toAct))
            finish()
        } catch (e: Exception) {
            MyLogger.logThis(
                TAG, "navigateToActivity()", "error launching activity ${e.message}",
                e
            )
        }
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