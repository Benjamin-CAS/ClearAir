package com.android_dev.cleanairspaces.ui.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.ActivitySplashBinding
import com.android_dev.cleanairspaces.ui.home.maps.AMapsActivity
import com.android_dev.cleanairspaces.ui.home.maps.GMapsActivity
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.showSnackBar
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashActViewModel by viewModels()
    private val TAG = SplashActivity::class.java.simpleName

    var snackBar: Snackbar? = null
    var popUp: AlertDialog? = null
    @Inject
    lateinit var myLogger: MyLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        viewModel.initDataRefresh()
        viewModel.getMapSelected().observe(
                this, Observer {
            if (it != null && it != getString(R.string.default_map_a_map)) {
                myLogger.logThis(TAG, "getMapSelected()", "google maps selected")
                if (checkForGooglePlayServices()) {
                    //send to google play services
                    navigateToActivity(GMapsActivity::class.java)
                } else {
                    myLogger.logThis(
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
                myLogger.logThis(TAG, "getMapSelected()", "a maps selected")
                navigateToActivity(AMapsActivity::class.java)
            }
        }
        )

    }

    private fun navigateToActivity(toAct: Class<*>) {
        try {
            startActivity(Intent(this, toAct))
            this.finish()
        } catch (e: Exception) {
            myLogger.logThis(
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

    fun showCustomDialog(ctx: Context, msgRes: Int, okRes: Int, dismissRes: Int, positiveAction: () -> Unit) {
        popUp?.let {
            if (it.isShowing) it.dismiss()
        }
        popUp = MaterialAlertDialogBuilder(ctx)
                .setTitle(msgRes)
                .setPositiveButton(
                        okRes
                ) { dialog, _ ->
                    positiveAction.invoke()
                    dialog.dismiss()
                }
                .setNeutralButton(
                        dismissRes
                ) { dialog, _ ->
                    dialog.dismiss()
                }.create()

        popUp?.show()
    }

    private fun dismissPopUps() {
        snackBar?.let {
            if (it.isShown)
                it.dismiss()
        }
        popUp?.let {
            if (it.isShowing) it.dismiss()
        }
        snackBar = null
        popUp = null
    }

    override fun onStop() {
        super.onStop()
        dismissPopUps()
    }
}