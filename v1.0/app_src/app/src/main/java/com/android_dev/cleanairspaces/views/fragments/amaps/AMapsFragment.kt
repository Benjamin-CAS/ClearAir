package com.android_dev.cleanairspaces.views.fragments.amaps

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentAMapsBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.MapData
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MY_LOCATION_ZOOM_LEVEL
import com.android_dev.cleanairspaces.utils.getAQIStatusFromPM25
import com.android_dev.cleanairspaces.views.adapters.WatchedLocationsAdapter
import com.android_dev.cleanairspaces.views.fragments.maps_overlay.BaseMapFragment
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AMapsFragment : BaseMapFragment() {

    companion object {
        private val TAG = AMapsFragment::class.java.simpleName
    }

    private val goodCircle: BitmapDescriptor  by lazy {  BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                    .decodeResource(resources, R.drawable.good_circle)
    ) }

    private val moderateCircle: BitmapDescriptor  by lazy {  BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                    .decodeResource(resources, R.drawable.moderate_circle)
    ) }

    private val gUnhealthyCircle: BitmapDescriptor  by lazy {  BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                    .decodeResource(resources, R.drawable.g_unhealthy_circle)
    )}

    private val unHealthyCircle: BitmapDescriptor  by lazy {  BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                    .decodeResource(resources, R.drawable.unhealthy_circle)
    )}

    private val vUnHealthyCircle: BitmapDescriptor  by lazy {  BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                    .decodeResource(resources, R.drawable.v_unhealthy_circle)
    ) }

    private val hazardCircle: BitmapDescriptor  by lazy {  BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                    .decodeResource(resources, R.drawable.hazardous_circle)
    )}


    private var _binding: FragmentAMapsBinding? = null
    private val binding get() = _binding!!


    private val mapCirlcesMarkers: ArrayList<Marker> = arrayListOf()
    private var mapView: MapView? = null
    private var aMap: AMap? = null


    /**
     * Initialize launchers for requesting permissions
     * must be called in OnCreate
     */
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

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAMapsBinding.inflate(inflater, container, false)
        watchedLocationsAdapter = WatchedLocationsAdapter(this)
        super.setHomeMapOverlay(binding.mapOverlay)

        setHasOptionsMenu(true)
        requireActivity().invalidateOptionsMenu()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLocationPermissionsLauncher()
        initQrScannerLauncher()

        //user setting - language
        viewModel.observeMapLang().observe(
                viewLifecycleOwner, {
            initializeMap(savedInstanceState = savedInstanceState, mapLangSet = it)
        }
        )

        viewModel.observeSelectedAqiIndex().observe(
                viewLifecycleOwner, {
            updateSelectedAqiIndex(it)
            observeWatchedLocations()
        }
        )

    }

    private fun observeWatchedLocations() {
        viewModel.observeWatchedLocations().observe(viewLifecycleOwner, {
            updateWatchedLocations(it)
        })
    }


    private var mapDataObserved = false
    private fun initializeMap(savedInstanceState: Bundle?, mapLangSet: String?) {
        binding.apply {
            mapView = map
            mapView?.let {
                aMap = it.map
                it.onCreate(savedInstanceState)
                aMap?.apply {
                    uiSettings?.isMyLocationButtonEnabled = false
                    isMyLocationEnabled = false
                    uiSettings.isZoomControlsEnabled = false
                    if (mapLangSet == null || mapLangSet == getString(R.string.map_lang_chinese)) {
                        setMapLanguage(AMap.CHINESE)
                    } else {
                        setMapLanguage(AMap.ENGLISH)
                    }
                    if (!mapDataObserved) {
                        observeMapRelatedData()
                        mapDataObserved = true
                    }
                }
            }
        }
    }

    private fun observeMapRelatedData() {
        //map data -locations & pm values
        viewModel.observeMapData().observe(
                viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                drawCirclesOnMap(it)
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
        val mIcon = BitmapDescriptorFactory.fromBitmap(
                BitmapFactory
                        .decodeResource(resources, R.drawable.ic_my_location_marker)
        )

        val markerOptions = MarkerOptions()
        markerOptions.apply {
            position(currentUserLocation)
            draggable(false)
            anchor(0.5f, 0.5f)
            mIcon?.let {
                icon(it)
            }
        }
        viewModel.myLocMarkerOnAMap = aMap?.addMarker(markerOptions)
    }


    private fun drawCirclesOnMap(mapDataPoints: List<MapData>) {
        try {
            clearMapCircles()
            aMap?.let {
                for (mapData in mapDataPoints) {
                    val aqiStatus = getAQIStatusFromPM25(mapData.pm25)
                    val mIcon = when (aqiStatus.level_intensity) {
                        1.0 -> goodCircle
                        2.0 -> moderateCircle
                        3.0 -> gUnhealthyCircle
                        4.0 -> unHealthyCircle
                        5.0 -> vUnHealthyCircle
                        else -> hazardCircle
                    }
                    val markerOptions = MarkerOptions()
                    markerOptions.apply {
                        val area = mapData.getAMapLocationLatLng()
                        position(area)
                        draggable(false)
                        anchor(0.5f, 0.5f)
                        mIcon.let {
                            icon(it)
                        }
                    }
                    val circleMarker = aMap?.addMarker(markerOptions)
                    circleMarker?.let { marker ->
                        mapCirlcesMarkers.add(marker)
                    }
                }
            }
        } catch (exc: Exception) {
            myLogger.logThis(tag = LogTags.EXCEPTION, from = "$TAG drawCirclesOnMap()", msg = exc.message, exc = exc)

        }
    }


    private fun clearMapCircles() {
        for (circle in mapCirlcesMarkers) {
            circle.remove()
        }
    }

    override fun handleScannedQrIntent(resultCode: Int, data: Intent?) {
        val intentResult = IntentIntegrator.parseActivityResult(resultCode, data)
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(
                        requireContext(),
                        R.string.scan_qr_code_cancelled,
                        Toast.LENGTH_LONG
                ).show()
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                val qrContent = intentResult.contents
                val action = AMapsFragmentDirections.actionAMapsFragmentToAddLocation(
                        locDataFromQr = qrContent
                )
                findNavController().navigate(action)
            }
        } else {
            Toast.makeText(
                    requireContext(), R.string.scan_qr_code_unknown,
                    Toast.LENGTH_LONG
            ).show()
        }
    }

    /************* forwarding life cycle methods & clearing *********/
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    private fun dismissPopUps() {
        snackBar?.let {
            if (it.isShown)
                it.dismiss()
        }

        popUp?.let {
            if (it.isShowing)
                it.dismiss()
        }

    }


    override fun onPause() {
        super.onPause()
        mapView?.onPause()
        dismissPopUps()
    }


    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }


    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()

    }


    /********************* NAVIGATION ***************/
    override fun goToSearchFragment() {
        try {
            val action = AMapsFragmentDirections.actionAMapsFragmentToSearchFragment()
            findNavController().navigate(action)
        } catch (exc: Exception) {
            myLogger.logThis(tag = LogTags.EXCEPTION, from = "$TAG goToSearchFragment()", msg = exc.message, exc = exc)


        }
    }

    override fun onClickWatchedLocation(location: WatchedLocationHighLights) {
        try {
            val action = AMapsFragmentDirections.actionAMapsFragmentToDetailsFragment(location)
            binding.container.findNavController().navigate(action)
        } catch (exc: Exception) {
            myLogger.logThis(tag = LogTags.EXCEPTION, from = "$TAG onClickWatchedLocation", msg = exc.message, exc = exc)
        }
    }
}