package com.cleanairspaces.android.ui.home.amap

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityAmapBinding
import com.cleanairspaces.android.models.entities.OutDoorLocations
import com.cleanairspaces.android.ui.home.BaseMapActivity
import com.cleanairspaces.android.ui.home.adapters.MapActionsAdapter
import com.cleanairspaces.android.ui.home.adapters.MyLocationsAdapter
import com.cleanairspaces.android.utils.AQI.getAQIWeightFromPM25
import com.cleanairspaces.android.utils.HEAT_MAP_CIRCLE_RADIUS
import com.cleanairspaces.android.utils.MyColorUtils.getGradientColors
import com.cleanairspaces.android.utils.MyColorUtils.getGradientIntensities
import com.cleanairspaces.android.utils.UPDATE_USER_LOCATION_INTERVAL
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AMapActivity : BaseMapActivity() {

    private var tileOverlay: TileOverlay? = null
    private lateinit var binding: ActivityAmapBinding

    private val TAG = AMapActivity::class.java.simpleName


    private var mapView: MapView? = null
    private var aMap: AMap? = null

    override val mapActionsAdapter: MapActionsAdapter by lazy {
        MapActionsAdapter(this)
    }
    override val myLocationsAdapter: MyLocationsAdapter by lazy {
        MyLocationsAdapter(this)
    }

    private fun initPermissionsLauncher() {
        //location permission launcher must be set
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showUserLocation()
            }
        }
    }

    private fun initQrScannerLauncher() {
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
        binding = ActivityAmapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initPermissionsLauncher()
        initQrScannerLauncher()

        super.setToolBar(binding.toolbarLayout, true)
        super.homeMapOverlay = binding.homeMapOverlay

        initializeMap(savedInstanceState)

        super.initializeRecyclerViewForUserActions()
        super.initializeMyLocationsRecycler()

        viewModel.getSelectedMapLang().observe(this, { selectedMapLang ->
            aMap?.let { map ->
                if (selectedMapLang.equals(getString(R.string.default_map_language))) {
                    map.setMapLanguage(AMap.CHINESE)
                } else {
                    map.setMapLanguage(AMap.ENGLISH)
                }
            }
        })
    }

    private fun initializeMap(savedInstanceState: Bundle?) {
        binding.apply {
            mapView = map
            mapView?.let { mMapView ->
                mMapView.onCreate(savedInstanceState)
                aMap = mMapView.map
                aMap?.apply {
                    uiSettings.isZoomControlsEnabled = false
                    onShowMyLocationOnMapClicked()
                    observeOutDoorLocations()
                }
            }
        }
    }

    private fun onShowMyLocationOnMapClicked() {
        binding.myLocationBtn.setOnClickListener {
            requestPermissionsToShowUserLocation()
        }
    }

    private fun observeOutDoorLocations() {
        viewModel.observeOutDoorLocations().observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                setupHeatMap(locations = it)
            }
        })
    }

    override fun showUserLocation() {
        if (!viewModel.hasSetMyLocationStyle) {
            viewModel.hasSetMyLocationStyle = true
            val myLocationStyle = MyLocationStyle()
            myLocationStyle.apply {
                myLocationIcon(
                    BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory
                            .decodeResource(resources, R.drawable.my_location_icon)
                    )
                )
                interval(UPDATE_USER_LOCATION_INTERVAL)
                myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER)
            }
            aMap?.apply {
                setMyLocationStyle(myLocationStyle)
                uiSettings?.isMyLocationButtonEnabled = false
                isMyLocationEnabled = true
                setOnMyLocationChangeListener {
                    viewModel.setUserLastKnownALatLng(it)
                }
            }
        }
        //gps is required
        super.promptMyLocationSettings()

        //change to last known location
        viewModel.getUserLastKnownALatLng()?.let {
            aMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    it,
                    10f
                )
            )
        }

    }


    /**************** MARKERS & CIRCLES & HEAT MAPS **************/
    private fun setupHeatMap(locations: List<OutDoorLocations>) {
        val locationsLatLng: MutableCollection<WeightedLatLng> = mutableListOf()
        for (location in locations) {
            val pm25: Double =
                (if (location.pm2p5.isNotBlank()) location.pm2p5 else location.reading).toDouble()
            locationsLatLng.add(
                WeightedLatLng(
                    location.getAMapLocationLatLng(),
                    getAQIWeightFromPM25(pm25).toDouble()
                )
            )
        }

        val gradient = Gradient(getGradientColors(), getGradientIntensities())
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
        super.observeMyLocations(this)
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
        dismissPopUps()
    }


    override fun gotToActivity(toAct: Class<*>) {
        startActivity(
            Intent(
                this,
                toAct
            )
        )

    }
}

