package com.cleanairspaces.android.ui.home.amap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import com.cleanairspaces.android.ui.home.MapActionChoices
import com.cleanairspaces.android.ui.home.MapViewModel
import com.cleanairspaces.android.ui.home.adapters.MapActionsAdapter
import com.cleanairspaces.android.utils.AQI
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.UIColor
import com.cleanairspaces.android.utils.showSnackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AMapActivity : AppCompatActivity(), MapActionsAdapter.ClickListener  {

    private lateinit var binding: ActivityAmapBinding

    private val TAG = AMapActivity::class.java.simpleName

    //prepare bitmaps
    private val aQIGoodBitmap = R.drawable.good_circle
    private val aQIModerateBitmap = R.drawable.moderate_circle
    private val aQIGUnhealthyBitmap = R.drawable.g_unhealthy_circle
    private val aQIUnhealthyBitmap = R.drawable.unhealthy_circle
    private val aQIVUnhealthyBitmap = R.drawable.v_unhealthy_circle
    private val aQIHazardousBitmap = R.drawable.hazardous_circle
    private val aQIBeyondBitmap = R.drawable.beyond_circle
    private val aQICNExcellentBitmap = R.drawable.excellent

    private val viewModel: MapViewModel by viewModels()
    private var popUp: AlertDialog? = null
    private var snackbar: Snackbar? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val mapActionsAdapter = MapActionsAdapter(this)

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

        private fun requestPermissionsToShowUserLocation() {
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

        /* TODO request location LAT-LNG and reposition camera instead ******/
        private fun showUserLocation() {
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
                promptMyLocationSettings()
            }
        }

        private fun promptMyLocationSettings() {
            if (viewModel.hasPromptedForLocationSettings)
                return
            viewModel.hasPromptedForLocationSettings = true
            val manager = getSystemService(LOCATION_SERVICE) as LocationManager?
            if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //showTurnOnGPSDialog
                showDialog(msgRes = R.string.turn_on_gps_prompt, positiveAction = { startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
            }
        }

        override fun onClickAction(actionChoice: MapActionChoices) {
            MyLogger.logThis(TAG, "onClickAction()", "user clicked ${getString(actionChoice.strRes)}")
        }


        /***************** DIALOGS ****************/
        private fun showDialog(msgRes: Int, positiveAction: () -> Unit) {
            dismissPopUps()
            popUp = MaterialAlertDialogBuilder( this)
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

        private fun showSnackBar(
                msgRes: Int,
                isError: Boolean = false,
                actionRes: Int? = null
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
                else -> super.onOptionsItemSelected(item)
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


        /************* forwarding life cycle methods & clearing *********/
        private fun dismissPopUps() {
            popUp?.let {
                if (it.isShowing) it.dismiss()
            }
            snackbar?.let {
                if (it.isShown) it.dismiss()
            }
            popUp = null
            snackbar = null
        }

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