package com.android_dev.cleanairspaces.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.BaseActivity
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.HomeMapOverlayBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.ui.adding_locations.add.CaptureQrCodeActivity
import com.android_dev.cleanairspaces.ui.adding_locations.search.SearchLocationAct
import com.android_dev.cleanairspaces.ui.details.LocationDetailsActivity
import com.android_dev.cleanairspaces.ui.home.adapters.WatchedLocationsAdapter
import com.android_dev.cleanairspaces.ui.settings.SettingsActivity
import com.android_dev.cleanairspaces.utils.*
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseMapAct : BaseActivity(), WatchedLocationsAdapter.OnClickItemListener {

    abstract fun navigateToActivity(toAct: Class<*>, extraTag: String?, data: Any?)

    private lateinit var homeMapOverlay: HomeMapOverlayBinding
    lateinit var watchedLocationsAdapter: WatchedLocationsAdapter
    fun setHomeMapOverlay(mapOverlay: HomeMapOverlayBinding) {
        homeMapOverlay = mapOverlay
        homeMapOverlay.apply {
            myLocationBtn.setOnClickListener {
                requestPermissionsAndShowUserLocation()
            }
            mapView.setOnClickListener {

                homeMapOverlay.myLocationsRv.isVisible = !homeMapOverlay.myLocationsRv.isVisible
            }
            smartQr.setOnClickListener {
                scanQRCode()
            }

            searchLocation.setOnClickListener {
                navigateToActivity(SearchLocationAct::class.java, null, null)
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
        }
    }

    val viewModel: BaseMapVieModel by viewModels()

    val heatMapGradientColors: List<AqiColors> =
        listOf<AqiColors>(
            AqiColors.AQIGoodColor,
            AqiColors.AQIModerateColor,
            AqiColors.AQIGUnhealthyColor,
            AqiColors.AQIUnhealthyColor,
            AqiColors.AQIVUnhealthyColor,
            AqiColors.AQIHazardousColor,
        )


    /*********** LOCATION ACCESS ***************/
    abstract fun showLocationOnMap(location: Location)
    fun promptToggleGPSSettings() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager?
        if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //showTurnOnGPSDialog
            showCustomDialog(msgRes = R.string.turn_on_gps_prompt,
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
            //TODO log location
            MyLogger.logThis(
                "BaseMapAct",
                "locationChanged()",
                "${location.latitude}, ${location.longitude} "
            )
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
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            USER_LOCATION_UPDATE_INTERVAL_MILLS,
            USER_LOCATION_UPDATE_ON_DISTANCE,
            locationListener
        )

    }

    private fun requestPermissionsAndShowUserLocation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            when {
                checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showUserLocation()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    showCustomDialog(
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


    /****************** MENU **************/
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.map_view_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.aboutAppMenu -> {

                //todo
                true
            }
            R.id.settingsMenu -> {
                navigateToActivity(
                    SettingsActivity::class.java, null, null
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    /************** qr code act **********/
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

    lateinit var scanQrCodeLauncher: ActivityResultLauncher<Intent>
    abstract fun initQrScannerLauncher()
    abstract fun handleScannedQrIntent(resultCode: Int, data: Intent?)


    /************** WATCHED LOCATIONS *********/
    override fun onClickWatchedLocation(location: WatchedLocationHighLights) {
        navigateToActivity(
            LocationDetailsActivity::class.java,
            extraTag = LocationDetailsActivity.INTENT_EXTRA_TAG,
            data = location
        )
    }

    fun updateWatchedLocations(watchedLocationHighLights: List<WatchedLocationHighLights>) {
        watchedLocationsAdapter.setWatchedLocationsList(watchedLocationHighLights)
    }

    fun updateSelectedAqiIndex(selectedAQIIndex: String?) {
        val aqiIndex = selectedAQIIndex ?: getString(R.string.default_aqi_pm_2_5)
        watchedLocationsAdapter.updateSelectedAqiIndex(aqiIndex)
    }

    private val TAG = BaseMapAct::class.java.simpleName
}
