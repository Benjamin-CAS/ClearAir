package com.android_dev.cleanairspaces.ui.home.maps

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.ActivityGoogleMapsBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.MapData
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.ui.adding_locations.add.AddLocationActivity
import com.android_dev.cleanairspaces.ui.home.BaseMapAct
import com.android_dev.cleanairspaces.ui.home.adapters.WatchedLocationsAdapter
import com.android_dev.cleanairspaces.utils.HEAT_MAP_CIRCLE_RADIUS
import com.android_dev.cleanairspaces.utils.MY_LOCATION_ZOOM_LEVEL
import com.android_dev.cleanairspaces.utils.getAQIStatusFromPM25
import com.android_dev.cleanairspaces.utils.pmLevelIntensities
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GMapsActivity : BaseMapAct(), OnMapReadyCallback {

    private val TAG = GMapsActivity::class.java.simpleName

    private var mMap: GoogleMap? = null

    private lateinit var binding: ActivityGoogleMapsBinding

    override fun initLocationPermissionsLauncher() {
        //location permission launcher must be set
        requestLocationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showUserLocation()
            }
        }
    }

    override fun initQrScannerLauncher() {
        //scan-qr launcher must be set
        scanQrCodeLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    //  you will get result here in result.data
                    handleScannedQrIntent(resultCode = result.resultCode, data = result.data)
                }

            }
    }

    private var tileOverlay: TileOverlay? = null
    private var formattedHeatMapGradientColors: IntArray? = null
    private fun setHeatMapGradientColors() {
        if (formattedHeatMapGradientColors?.isNotEmpty() == true) return
        formattedHeatMapGradientColors = IntArray(heatMapGradientColors.size)
        for ((i, aqiColor) in heatMapGradientColors.withIndex()) {
            formattedHeatMapGradientColors!![i] =
                ContextCompat.getColor(this, aqiColor.colorRes)

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleMapsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initLocationPermissionsLauncher()
        initQrScannerLauncher()

        super.setToolBar(binding.toolbar, true)
        watchedLocationsAdapter = WatchedLocationsAdapter(this)
        super.setHomeMapOverlay(binding.mapOverlay)

        /*********** OBSERVE MAP OVERLAY DATA **********/
        viewModel.observeWatchedLocations().observe(this, Observer {
            updateWatchedLocations(it)
        })
        viewModel.observeSelectedAqiIndex().observe(
            this, Observer {
                updateSelectedAqiIndex(it)
            }
        )

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.g_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setHeatMapGradientColors()
        observeMapRelatedData()
    }

    private fun observeMapRelatedData() {
        //map data -locations & pm values
        viewModel.observeMapData().observe(
            this, Observer {
                if (it.isNotEmpty()) {
                    drawHeatMap(it)
                }
            }
        )
    }

    private fun drawHeatMap(mapDataPoints: List<MapData>) {
        val locationsLatLng: MutableCollection<WeightedLatLng> = mutableListOf()
        for (mapData in mapDataPoints) {
            val weight = getAQIStatusFromPM25(mapData.pm25).level_intensity
            locationsLatLng.add(
                WeightedLatLng(
                    mapData.getGMapLocationLatLng(),
                    weight
                )
            )
        }

        val gradient = Gradient(formattedHeatMapGradientColors, pmLevelIntensities)
        val builder = HeatmapTileProvider.Builder()
        builder.apply {
            weightedData(locationsLatLng)
            gradient(gradient)
            radius(HEAT_MAP_CIRCLE_RADIUS)
        }
        tileOverlay?.clearTileCache()
        tileOverlay?.remove()
        val heatMapTileProvider: HeatmapTileProvider = builder.build()
        val tileOverlayOptions = TileOverlayOptions()
        tileOverlayOptions.tileProvider(heatMapTileProvider)
        tileOverlay = mMap?.addTileOverlay(tileOverlayOptions)
    }

    override fun showLocationOnMap(location: Location) {
        val currentUserLocation = LatLng(location.latitude, location.longitude)
        mMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                currentUserLocation,
                MY_LOCATION_ZOOM_LEVEL
            )
        )
        viewModel.myLocMarkerOnGMap?.remove()
        viewModel.myLocMarkerOnGMap = mMap?.addMarker(
            MarkerOptions().position(currentUserLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_marker))
        )
    }

    override fun navigateToActivity(toAct: Class<*>, extraTag: String?, data: Any?) {
        when {
            extraTag != null && data is String -> startActivity(
                Intent(this, toAct).putExtra(
                    extraTag,
                    data
                )
            )
            extraTag != null && data is WatchedLocationHighLights -> startActivity(
                Intent(
                    this,
                    toAct
                ).putExtra(extraTag, data)
            )
            else -> startActivity(Intent(this, toAct))
        }
    }

    override fun onResume() {
        super.onResume()
        mMap?.let {
            observeMapRelatedData()
        }
    }


    override fun handleScannedQrIntent(resultCode: Int, data: Intent?) {
        val intentResult = IntentIntegrator.parseActivityResult(resultCode, data)
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(
                    this,
                    R.string.scan_qr_code_cancelled,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                val qrContent = intentResult.contents
                val extraTag = AddLocationActivity.INTENT_FROM_QR_SCANNER_TAG
                navigateToActivity(
                    AddLocationActivity::class.java,
                    extraTag = extraTag, data = qrContent
                )
            }
        } else {
            Toast.makeText(
                this, R.string.scan_qr_code_unknown,
                Toast.LENGTH_LONG
            ).show()
        }
    }

}