package com.cleanairspaces.android.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.HomeMapOverlayBinding
import com.cleanairspaces.android.models.entities.LocationDetailsGeneralDataWrapper
import com.cleanairspaces.android.ui.BaseActivity
import com.cleanairspaces.android.ui.about.AboutAppActivity
import com.cleanairspaces.android.ui.search_locations.SearchLocationActivity
import com.cleanairspaces.android.ui.details.LocationDetailsActivity
import com.cleanairspaces.android.ui.home.adapters.MapActionsAdapter
import com.cleanairspaces.android.ui.home.adapters.MyLocationsAdapter
import com.cleanairspaces.android.ui.settings.SettingsActivity
import com.cleanairspaces.android.ui.add_locations.CaptureQrCodeActivity
import com.cleanairspaces.android.ui.add_locations.AddLocationActivity
import com.cleanairspaces.android.ui.add_locations.AddLocationActivity.Companion.INTENT_EXTRA_QR_CONTENT_TAG
import com.cleanairspaces.android.utils.*
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
    val viewModel: BaseMapViewModel by viewModels()

    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    lateinit var scanQrCodeLauncher: ActivityResultLauncher<Intent>
    abstract val mapActionsAdapter: MapActionsAdapter
    abstract val myLocationsAdapter: MyLocationsAdapter
    lateinit var homeMapOverlay: HomeMapOverlayBinding

    abstract fun showUserLocation()
    abstract fun gotToActivity(toAct: Class<*>)

    /*********** ADD, SCAN & TOGGLE ACTIONS *********/
    private fun hideMyLocationsView() {
        homeMapOverlay.apply {
            if (locationsRv.visibility == View.VISIBLE)
                locationsRv.visibility = View.INVISIBLE
            else
                locationsRv.visibility = View.VISIBLE
        }
    }

    fun initializeRecyclerViewForUserActions(
    ) {
        homeMapOverlay.apply {
            mapActionsRv.layoutManager = LinearLayoutManager(
                this@BaseMapActivity,
                RecyclerView.HORIZONTAL,
                false
            )
            mapActionsAdapter.setMapActionsList(viewModel.mapActions)
            mapActionsRv.adapter = mapActionsAdapter
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
                hideMyLocationsView()
            }
            MapActionChoices.ADD -> {
                gotToActivity(SearchLocationActivity::class.java)
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
                startActivity(
                    Intent(this@BaseMapActivity, AddLocationActivity::class.java).putExtra(
                        INTENT_EXTRA_QR_CONTENT_TAG, qrContent
                    )
                )

            }
        } else {
            showSnackBar(msgRes = R.string.scan_qr_code_unknown)
        }
    }

    /***************** DIALOGS ****************/
    private fun showSnackBar(
        msgRes: Int,
        isError: Boolean = false,
        actionRes: Int? = null
    ) {
        dismissPopUps()

        snackbar = homeMapOverlay.viewsContainer.showSnackBar(
            msgResId = msgRes,
            isErrorMsg = isError,
            actionMessage = actionRes
        )
    }


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
                /*  R.id.action_help -> {
                showDialog(msgRes = R.string.map_menu_help_desc_txt, positiveAction = {})
                true
            } */
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

    /********* OBSERVE MY LOCATIONS DATA  ****/
    private fun updateMyLocationsListForAdapter(myLocations: List<LocationDetailsGeneralDataWrapper>) {
        myLocationsAdapter.setMyLocationsList(myLocationsList = myLocations)
    }

    fun observeMyLocations(owner: LifecycleOwner) {
        viewModel.getSelectedAqiIndex().observe(owner, Observer {
            myLocationsAdapter.setAQIIndex(it)
        })
        viewModel.refreshMyLocationsFlow().observe(owner, Observer {
            if (it != null) {
                viewModel.updateMyLocationsDetailsWrapper(it)
            }
        })

        viewModel.observeMyLocationDetailsWrapper().observe(owner, Observer {
            if (it != null) {
                updateMyLocationsListForAdapter(it)
            }
        })

    }


    /************** MY LOCATIONS *********/
    override fun onClickLocation(myLocationDetails: MyLocationDetailsWrapper) {
        startActivity(
            Intent(this, LocationDetailsActivity::class.java).putExtra(
                LocationDetailsActivity.INTENT_EXTRA_TAG,
                myLocationDetails
            )
        )
    }

    fun initializeMyLocationsRecycler() {
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

}