package com.cleanairspaces.android.ui.home.amap

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.MapView
import com.amap.api.maps2d.model.BitmapDescriptorFactory
import com.amap.api.maps2d.model.MarkerOptions
import com.amap.api.maps2d.model.MyLocationStyle
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityAmapBinding
import com.cleanairspaces.android.models.entities.OutDoorLocations
import com.cleanairspaces.android.ui.home.BaseMapActivity
import com.cleanairspaces.android.ui.home.MapViewModel
import com.cleanairspaces.android.utils.AQI
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.UIColor
import com.cleanairspaces.android.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AMapActivity : BaseMapActivity()  {

    private lateinit var binding: ActivityAmapBinding

    private val TAG = AMapActivity::class.java.simpleName

    private val viewModel: MapViewModel by viewModels()

    private var mapView: MapView? = null
    private var aMap: AMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amap)
        binding = ActivityAmapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.apply {
            toolbar.isVisible = true
            toolbarTitle.isVisible = false
            toolbarLogo.isVisible = true
            Glide.with(this@AMapActivity)
                    .load(R.drawable.clean_air_spaces_logo_name)
                    .into(toolbarLogo)
        }

        //location permission launcher must be set
        requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showUserLocation()
            }
        }

        initializeRecyclerViewForUserActions()
        initializeMap(savedInstanceState)
    }

    /*************** USER ACTIONS ****************/
    private fun initializeRecyclerViewForUserActions(){
        binding.apply {
            mapActionsRv.layoutManager = LinearLayoutManager(
                    this@AMapActivity,
                    RecyclerView.HORIZONTAL,
                    false
            )
            mapActionsAdapter.setMapActionsList(viewModel.mapActions)
            mapActionsRv.adapter = mapActionsAdapter
        }

    }

    private fun initializeMap(savedInstanceState: Bundle?) {
        binding.apply {
            mapView = map
            mapView?.let { mMapView ->
                mMapView.onCreate(savedInstanceState)
                aMap = mMapView.map
                aMap?.apply {
                    //todo settings setMapLanguage(AMap.ENGLISH)
                    uiSettings.isZoomControlsEnabled = false
                    requestPermissionsToShowUserLocation()
                    observeOutDoorLocations()
                }
            }
        }

    }

    private fun observeOutDoorLocations() {
        viewModel.observeLocations().observe(this, Observer {
            if (it != null) {
                MyLogger.logThis(
                        TAG, "observeOutDoorLocations() -> observeLocations()", " found in room ${it.size}"
                )
                aMap?.clear()
                setupMarkers(locations = it)
            }
        })
    }


        /****************** USER LOCATION ******************/
        /* TODO request location LAT-LNG and reposition camera instead ******/
        override fun showUserLocation() {
            val myLocationStyle = MyLocationStyle()
            myLocationStyle.apply {
                myLocationIcon(
                        BitmapDescriptorFactory.fromBitmap(
                                BitmapFactory
                                        .decodeResource(resources, R.drawable.my_location_icon)
                        )
                )
                interval(2000)
                myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
            }
            aMap?.apply {
                setMyLocationStyle(myLocationStyle)
                uiSettings?.isMyLocationButtonEnabled = false
                isMyLocationEnabled = true
                askUserToEnableGPS()
            }
        }

        private fun  askUserToEnableGPS(){
            if (viewModel.hasPromptedForLocationSettings)
                return
            viewModel.hasPromptedForLocationSettings = true
            promptMyLocationSettings()
        }


    override fun hideMyLocations() {
        binding.apply {
            if (locationsRv.visibility == View.VISIBLE)
                locationsRv.visibility = View.INVISIBLE
            else
                locationsRv.visibility = View.VISIBLE
        }
    }





        /**************** MARKERS & CIRCLES **************/
        private fun setupMarkers(locations: List<OutDoorLocations>) {
            for (location in locations) {
                val mDrawable = getIconForMarker(location)
                val mIcon = if (mDrawable == null) null else
                    BitmapDescriptorFactory.fromBitmap(
                            BitmapFactory
                                    .decodeResource(resources, mDrawable)
                    )

                val markerOptions = MarkerOptions()
                markerOptions.apply {
                    position(location.getAMapLocationLatLng())
                    draggable(false)
                    anchor(0.5f, 0.5f)
                    mIcon?.let{
                        icon(it)
                    }
                }
                aMap?.addMarker(markerOptions)
            }
            aMap?.setOnMarkerClickListener {
                false
            }

        }

        private fun getIconForMarker(location: OutDoorLocations): Int? {
            val pm25 = if (location.pm2p5 != "") location.pm2p5 else location.reading
            return when (AQI.getAQIColorFromPM25(pm25.toDouble())) {
                UIColor.AQIGoodColor -> aQIGoodBitmap
                UIColor.AQIModerateColor -> aQIModerateBitmap
                UIColor.AQIGUnhealthyColor -> aQIGUnhealthyBitmap
                UIColor.AQIUnhealthyColor -> aQIUnhealthyBitmap
                UIColor.AQIVUnhealthyColor -> aQIVUnhealthyBitmap
                UIColor.AQIHazardousColor -> aQIHazardousBitmap
                UIColor.AQIBeyondColor -> aQIBeyondBitmap
                UIColor.AQICNExcellentColor -> aQICNExcellentBitmap
                else -> null
            }
        }


    /************************** QR CODE SCANNING ***************/


        /***** dialogs ******/
    override fun showSnackBar(
                msgRes: Int,
                isError: Boolean,
                actionRes: Int?
        ) {
            dismissPopUps()
            binding.apply {
                snackbar = viewsContainer.showSnackBar(
                        msgResId = msgRes,
                        isErrorMsg = isError,
                        actionMessage = actionRes
                )
            }
        }

    /************* forwarding life cycle methods & clearing *********/
        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            mapView?.onSaveInstanceState(outState)
        }

        override fun onResume() {
            super.onResume()
            mapView?.onResume()
        }

        override fun onPause() {
            super.onPause()
            mapView?.onPause()
        }


    override fun onDestroy() {
        super.onDestroy()
            mapView?.onDestroy()
            dismissPopUps()
        }
}