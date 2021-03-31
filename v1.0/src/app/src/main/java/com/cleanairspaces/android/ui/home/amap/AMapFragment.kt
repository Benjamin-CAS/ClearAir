package com.cleanairspaces.android.ui.home.amap


import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.MapView
import com.amap.api.maps2d.model.*
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.FragmentAmapBinding
import com.cleanairspaces.android.models.entities.OutDoorLocations
import com.cleanairspaces.android.ui.home.adapters.MapActionsAdapter
import com.cleanairspaces.android.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AMapFragment : Fragment(), MapActionsAdapter.ClickListener {

    companion object {
        private val TAG = AMapFragment::class.java.simpleName
    }

    //prepare bitmaps
    private val aQIGoodBitmap = R.drawable.good_circle
    private val aQIModerateBitmap = R.drawable.moderate_circle
    private val aQIGUnhealthyBitmap = R.drawable.g_unhealthy_circle
    private val aQIUnhealthyBitmap = R.drawable.unhealthy_circle
    private val aQIVUnhealthyBitmap = R.drawable.v_unhealthy_circle
    private val aQIHazardousBitmap = R.drawable.hazardous_circle
    private val aQIBeyondBitmap = R.drawable.beyond_circle
    private val aQICNExcellentBitmap = R.drawable.excellent


    private var _binding: FragmentAmapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AMapViewModel by viewModels()

    private var popUp: AlertDialog? = null
    private var snackbar: Snackbar? = null

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val mapActionsAdapter = MapActionsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showUserLocation()
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAmapBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            mapActionsRv.layoutManager = LinearLayoutManager(
                    requireContext(),
                    RecyclerView.HORIZONTAL,
                    false
            )
            mapActionsAdapter.setMapActionsList(viewModel.mapActions)
            mapActionsRv.adapter = mapActionsAdapter
        }

        initializeMap(savedInstanceState)

    }

    private var mapView: MapView? = null
    private var aMap: AMap? = null
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
        viewModel.refreshOutDoorLocations()
        viewModel.observeLocations().observe(viewLifecycleOwner, Observer {
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
        when {
            ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                showUserLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showDialog(msgRes = R.string.location_permission_rationale) { this@AMapFragment.requestPermission() }
            }
            else -> {
                requestPermission()
            }
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

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
        val manager = context?.getSystemService(LOCATION_SERVICE) as LocationManager?
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
        popUp = MaterialAlertDialogBuilder(requireContext())
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


    /************* MENU **************/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_view_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                showDialog(msgRes = R.string.map_menu_help_desc_txt, positiveAction = {})
                true
            }
            else -> item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(
                    item
            )
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
                position(location.getLocationLatLng())
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

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        dismissPopUps()
        _binding = null
    }
}


