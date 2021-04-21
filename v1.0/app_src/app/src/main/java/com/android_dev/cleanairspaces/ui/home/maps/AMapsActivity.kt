package com.android_dev.cleanairspaces.ui.home.maps

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.ActivityAMapsBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.MapData
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.ui.adding_locations.add.AddLocationActivity
import com.android_dev.cleanairspaces.ui.home.BaseMapAct
import com.android_dev.cleanairspaces.ui.home.adapters.WatchedLocationsAdapter
import com.android_dev.cleanairspaces.utils.HEAT_MAP_CIRCLE_RADIUS
import com.android_dev.cleanairspaces.utils.MY_LOCATION_ZOOM_LEVEL
import com.android_dev.cleanairspaces.utils.getAQIStatusFromPM25
import com.android_dev.cleanairspaces.utils.pmLevelIntensities
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AMapsActivity : BaseMapAct() {

    private var tileOverlay: TileOverlay? = null
    private val TAG = AMapsActivity::class.java.simpleName


    private lateinit var binding: ActivityAMapsBinding


    private var mapView: MapView? = null
    private var aMap: AMap? = null

    private var formattedHeatMapGradientColors: IntArray? = null
    private fun setHeatMapGradientColors() {
        if (formattedHeatMapGradientColors?.isNotEmpty() == true) return
        formattedHeatMapGradientColors = IntArray(heatMapGradientColors.size)
        for ((i, aqiColor) in heatMapGradientColors.withIndex()) {
            formattedHeatMapGradientColors!![i] =
                ContextCompat.getColor(this, aqiColor.colorRes)

        }
    }


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAMapsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initLocationPermissionsLauncher()
        initQrScannerLauncher()


        super.setToolBar(binding.toolbar, true)
        watchedLocationsAdapter = WatchedLocationsAdapter(this)
        super.setHomeMapOverlay(binding.mapOverlay)
        viewModel.observeWatchedLocations().observe(this, Observer {
            updateWatchedLocations(it)
        })
        viewModel.observeSelectedAqiIndex().observe(
            this, Observer {
                updateSelectedAqiIndex(it)
            }
        )

        setHeatMapGradientColors()
        initializeMap(savedInstanceState = savedInstanceState)
    }


    private fun initializeMap(savedInstanceState: Bundle?) {
        binding.apply {
            mapView = map
            mapView?.let { mMapView ->
                mMapView.onCreate(savedInstanceState)
                aMap = mMapView.map
                aMap?.apply {
                    uiSettings?.isMyLocationButtonEnabled = false
                    isMyLocationEnabled = false
                    uiSettings.isZoomControlsEnabled = false
                    observeMapRelatedData()
                }
            }
        }
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
        //user setting - language
        viewModel.observeMapLang().observe(
            this, Observer {
                if (it == null || it == getString(R.string.map_lang_chinese)) {
                    aMap?.setMapLanguage(AMap.CHINESE)
                } else {
                    aMap?.setMapLanguage(AMap.ENGLISH)
                }
            }
        )
    }

    override fun showLocationOnMap(location: Location) {
        val currentUserLocation = LatLng(location.latitude, location.longitude)
        aMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                currentUserLocation,
                MY_LOCATION_ZOOM_LEVEL
            )
        )
        viewModel.myLocMarkerOnAMap?.remove()
        val markerOptions = MarkerOptions().position(currentUserLocation)
            .icon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory
                        .decodeResource(resources, R.drawable.ic_my_location_marker)
                )
            )
        viewModel.myLocMarkerOnAMap = aMap?.addMarker(markerOptions)
    }

    /**************** HEAT MAPS **************/
    private fun drawHeatMap(mapDataPoints: List<MapData>) {
        val locationsLatLng: MutableCollection<WeightedLatLng> = mutableListOf()
        for (mapData in mapDataPoints) {
            val weight = getAQIStatusFromPM25(mapData.pm25).level_intensity
            locationsLatLng.add(
                WeightedLatLng(
                    mapData.getAMapLocationLatLng(),
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
        tileOverlay = aMap?.addTileOverlay(tileOverlayOptions)
    }


    /************* forwarding life cycle methods & clearing *********/
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        aMap?.apply {
            observeMapRelatedData()
        }
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()

    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()

    }


    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
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