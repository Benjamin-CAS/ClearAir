package com.cleanairspaces.android.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.HomeMapOverlayBinding
import com.cleanairspaces.android.models.entities.CustomerDeviceDataDetailed
import com.cleanairspaces.android.ui.BaseActivity
import com.cleanairspaces.android.ui.about.AboutAppActivity
import com.cleanairspaces.android.ui.details.LocationDetailsActivity
import com.cleanairspaces.android.ui.home.adapters.home.MapActionsAdapter
import com.cleanairspaces.android.ui.home.adapters.home.MyLocationsAdapter
import com.cleanairspaces.android.ui.settings.SettingsActivity
import com.cleanairspaces.android.ui.smart_qr.CaptureQrCodeActivity
import com.cleanairspaces.android.ui.smart_qr.QrCodeProcessingActivity
import com.cleanairspaces.android.ui.smart_qr.QrCodeProcessingActivity.Companion.INTENT_EXTRA_TAG
import com.cleanairspaces.android.utils.LocationDetailsInfo
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.SCANNING_QR_TIMEOUT_MILLS
import com.cleanairspaces.android.utils.VerticalSpaceItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
abstract class BaseMapActivity : BaseActivity(), MapActionsAdapter.ClickListener,
    MyLocationsAdapter.MyLocationsClickListener {

    private val TAG = BaseMapActivity::class.java.simpleName



    var popUp: AlertDialog? = null
    var snackbar: Snackbar? = null

    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    lateinit var scanQrCodeLauncher: ActivityResultLauncher<Intent>
    abstract val mapActionsAdapter: MapActionsAdapter
    abstract val myLocationsAdapter: MyLocationsAdapter

    /** BE IMPLEMENTED **/
    abstract fun showUserLocation()
    abstract fun hideMyLocations()
    abstract fun showSnackBar(
        msgRes: Int,
        isError: Boolean = false,
        actionRes: Int? = null
    )

    abstract fun gotToActivity(toAct: Class<*>)

    /*********** ADD, SCAN & TOGGLE ACTIONS *********/
    fun initializeRecyclerViewForUserActions(
        homeMapOverlay: HomeMapOverlayBinding,
        actions: List<MapActions>
    ) {
        homeMapOverlay.apply {
            mapActionsRv.layoutManager = LinearLayoutManager(
                this@BaseMapActivity,
                RecyclerView.HORIZONTAL,
                false
            )
            mapActionsAdapter.setMapActionsList(actions)
            mapActionsRv.adapter = mapActionsAdapter
        }

    }

    /************** MY LOCATIONS *********/
    fun updateMyLocationsList(myLocations: List<CustomerDeviceDataDetailed>) {
        myLocationsAdapter.setMyLocationsList(myLocationsList = myLocations)
    }

    override fun onClickLocation(locationDetails: LocationDetailsInfo) {
        startActivity(Intent(this, LocationDetailsActivity::class.java).putExtra(LocationDetailsActivity.INTENT_EXTRA_TAG, locationDetails))
    }

    fun initializeMyLocationRecycler(homeMapOverlay: HomeMapOverlayBinding) {
        homeMapOverlay.apply {
            locationsRv.layoutManager = LinearLayoutManager(
                this@BaseMapActivity,
                RecyclerView.VERTICAL,
                false
            )
            locationsRv.addItemDecoration(VerticalSpaceItemDecoration(30))
            locationsRv.adapter = myLocationsAdapter
        }
    }

    /********* PERMISSIONS ***************/
    fun requestPermissionsToShowUserLocation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showUserLocation()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    showDialog(msgRes = R.string.location_permission_rationale) { requestPermission() }
                }
                else -> {
                    requestPermission()
                }
            }
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    /*********** USER ACTIONS *******/
    override fun onClickAction(actionChoice: MapActionChoices) {
        when (actionChoice) {
            MapActionChoices.SMART_QR -> {
                scanQRCode()
            }
            MapActionChoices.MAP_VIEW -> {
                hideMyLocations()
            }
            MapActionChoices.ADD -> {
                //todo
            }
        }
    }


    /************ QR CODE ***********/
    private fun scanQRCode() {
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.apply {
            val scanQrCodePromptText = getString(R.string.scan_qr_code_prompt)
            setPrompt(scanQrCodePromptText)
            setTimeout(SCANNING_QR_TIMEOUT_MILLS)
            setOrientationLocked(true)
            captureActivity = CaptureQrCodeActivity::class.java
        }
        val intent = intentIntegrator.createScanIntent()
        scanQrCodeLauncher.launch(intent)
    }

    fun handleScannedQrIntent(resultCode: Int, data: Intent?) {
        val intentResult = IntentIntegrator.parseActivityResult(resultCode, data)
        if (intentResult != null) {
            if (intentResult.contents == null) {
                showSnackBar(msgRes = R.string.scan_qr_code_cancelled)
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                val qrContent = intentResult.contents
                val qrFormatName = intentResult.formatName
                MyLogger.logThis(
                    TAG, " handleScannedQrIntent($resultCode : Int, $data : Intent?)",
                    "qrContent $qrContent , qrFormatName $qrFormatName"
                )
                startActivity(
                    Intent(this@BaseMapActivity, QrCodeProcessingActivity::class.java).putExtra(
                        INTENT_EXTRA_TAG, qrContent
                    )
                )

            }
        } else {
            MyLogger.logThis(
                TAG, " handleScannedQrIntent($resultCode : Int, $data : Intent?)",
                "qrContent $intentResult is null"
            )
            showSnackBar(msgRes = R.string.scan_qr_code_unknown)
        }
    }

    /***************** DIALOGS ****************/
    fun promptMyLocationSettings() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager?
        if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //showTurnOnGPSDialog
            showDialog(msgRes = R.string.turn_on_gps_prompt, positiveAction = {
                startActivity(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                )
            })
        }
    }

    fun dismissPopUps() {
        popUp?.let {
            if (it.isShowing) it.dismiss()
        }
        snackbar?.let {
            if (it.isShown) it.dismiss()
        }
        popUp = null
        snackbar = null
    }

    private fun showDialog(msgRes: Int, positiveAction: () -> Unit) {
        dismissPopUps()
        popUp = MaterialAlertDialogBuilder(this)
            .setTitle(msgRes)
            .setPositiveButton(
                R.string.got_it
            ) { dialog, _ ->
                positiveAction.invoke()
                dialog.dismiss()
            }
            .setNeutralButton(
                R.string.dismiss
            ) { dialog, _ ->
                dialog.dismiss()
            }.create()

        popUp?.show()
    }


    /****************** MENU **************/
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.map_view_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                showDialog(msgRes = R.string.map_menu_help_desc_txt, positiveAction = {})
                true
            }
            R.id.aboutAppMenu -> {

                gotToActivity(AboutAppActivity::class.java)
                true
            }
            R.id.settingsMenu -> {
                gotToActivity(SettingsActivity::class.java)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}