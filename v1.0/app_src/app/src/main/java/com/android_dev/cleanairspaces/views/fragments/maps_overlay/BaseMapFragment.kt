package com.android_dev.cleanairspaces.views.fragments.maps_overlay

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.HomeMapOverlayBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.*
import com.android_dev.cleanairspaces.views.adapters.WatchedLocationsAdapter
import com.android_dev.cleanairspaces.views.smartqr.CaptureQrCodeActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseMapFragment : Fragment(), WatchedLocationsAdapter.OnClickItemListener {
    companion object {
        private val TAG = BaseMapFragment::class.java.simpleName
    }

    @Inject
    lateinit var myLogger: MyLogger

    var snackBar: Snackbar? = null
    var popUp: AlertDialog? = null

    val viewModel: MapsViewModel by viewModels()

    abstract fun goToSearchFragment()

    private lateinit var homeMapOverlay: HomeMapOverlayBinding
    lateinit var watchedLocationsAdapter: WatchedLocationsAdapter


    fun setHomeMapOverlay(mapOverlay: HomeMapOverlayBinding) {
        homeMapOverlay = mapOverlay
        homeMapOverlay.apply {
            myLocationBtn.setOnClickListener {
                requestPermissionsAndShowUserLocation()
            }
            mapView.setOnClickListener {
                myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "MAP VIEW TOGGLE"
                )
                homeMapOverlay.myLocationsRv.isVisible = !homeMapOverlay.myLocationsRv.isVisible
            }
            smartQr.setOnClickListener {
                myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "SMART QR"
                )
                scanQRCode()
            }

            searchLocation.setOnClickListener {
                myLogger.logThis(
                        tag = LogTags.USER_ACTION_CLICK_FEATURE,
                        from = TAG,
                        msg = "SEARCH"
                )
                goToSearchFragment()
            }

            myLocationsRv.apply {
                layoutManager = LinearLayoutManager(
                        homeMapOverlay.container.context,
                        RecyclerView.VERTICAL,
                        false
                )
                adapter = watchedLocationsAdapter
                addItemDecoration(VerticalSpaceItemDecoration(30))
            }
            val swipeHandler = object : SwipeToDeleteCallback(myLocationsRv.context) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = myLocationsRv.adapter as WatchedLocationsAdapter
                    adapter.removeAt(viewHolder.adapterPosition)
                }

            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(myLocationsRv)
        }
    }

    /*********** LOCATION ACCESS ***************/
    abstract fun showLocationOnMap(location: Location)
    fun promptToggleGPSSettings() {
        val manager =
                requireContext().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager?
        if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //showTurnOnGPSDialog
            showCustomDialog(
                    ctx = requireContext(),
                    msgRes = R.string.turn_on_gps_prompt,
                    okRes = R.string.go_to_settings,
                    dismissRes = R.string.not_now_txt,
                    positiveAction = {
                        startActivity(
                                Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                                )
                        )
                    })
        }
    }

    private var locationManager: LocationManager? = null
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            viewModel.onLocationChanged(location)
            showLocationOnMap(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {
            if (!viewModel.alreadyPromptedUserForGPS) {
                viewModel.alreadyPromptedUserForGPS = true
                promptToggleGPSSettings()
            }
        }
    }
    lateinit var requestLocationPermissionLauncher: ActivityResultLauncher<String>
    abstract fun initLocationPermissionsLauncher()

    @SuppressLint("MissingPermission")
    fun showUserLocation() {
        locationManager =
                requireContext().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager?
        locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                USER_LOCATION_UPDATE_INTERVAL_MILLS,
                USER_LOCATION_UPDATE_ON_DISTANCE,
                locationListener
        )

    }

   fun requestPermissionsAndShowUserLocation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showUserLocation()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    showCustomDialog(
                            ctx = requireContext(),
                            msgRes = R.string.location_permission_rationale,
                            okRes = R.string.got_it_txt,
                            dismissRes = R.string.not_now_txt
                    ) {
                        requestPermission(isLocation = true)
                    }
                }
                else -> {
                    requestPermission(isLocation = true)
                }
            }
        }
    }

    private fun requestPermission(isLocation: Boolean) {
        if (isLocation)
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    /******************MENU **************/
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.save).isVisible = false
        menu.findItem(R.id.settingsMenuFragment).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(
                item
        )
    }

    /************** qr code act **********/
    private fun scanQRCode() {
        val intentIntegrator = IntentIntegrator(requireActivity())
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

    lateinit var scanQrCodeLauncher: ActivityResultLauncher<Intent>
    abstract fun initQrScannerLauncher()
    abstract fun handleScannedQrIntent(resultCode: Int, data: Intent?)


    /************** WATCHED LOCATIONS *********/
    override fun onSwipeToDeleteLocation(location: WatchedLocationHighLights) {
        viewModel.deleteLocation(location)
    }

    fun updateWatchedLocations(watchedLocationHighLights: List<WatchedLocationHighLights>) {
        watchedLocationsAdapter.setWatchedLocationsList(watchedLocationHighLights)
    }

    fun updateSelectedAqiIndex(selectedAQIIndex: String?) {
        val aqiIndex = selectedAQIIndex ?: getString(R.string.default_aqi_pm_2_5)
        watchedLocationsAdapter.updateSelectedAqiIndex(aqiIndex)
    }

    private fun showCustomDialog(
            ctx: Context,
            msgRes: Int,
            okRes: Int,
            dismissRes: Int,
            positiveAction: () -> Unit
    ) {
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

}