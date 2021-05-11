package com.android_dev.cleanairspaces.views.fragments.gmaps

import android.app.Activity
import android.content.Intent
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
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentGMapsBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.MapData
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MY_LOCATION_ZOOM_LEVEL
import com.android_dev.cleanairspaces.utils.getAQIStatusFromPM25
import com.android_dev.cleanairspaces.utils.showSnackBar
import com.android_dev.cleanairspaces.views.adapters.MonitorsAdapter
import com.android_dev.cleanairspaces.views.adapters.WatchedLocationsAdapter
import com.android_dev.cleanairspaces.views.fragments.maps_overlay.BaseMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GMapsFragment : BaseMapFragment(), OnMapReadyCallback {

    companion object {
        private val TAG = GMapsFragment::class.java.simpleName
    }

    private var _binding: FragmentGMapsBinding? = null
    private val binding get() = _binding!!

    private var mMap: GoogleMap? = null
    private val mapCircleMarkers: ArrayList<Marker> = arrayListOf()


    private val goodCircle by lazy {   BitmapDescriptorFactory.fromResource(R.drawable.good_circle)
   }

    private val moderateCircle by lazy {   BitmapDescriptorFactory.fromResource(R.drawable.moderate_circle)
   }

    private val gUnhealthyCircle by lazy {   BitmapDescriptorFactory.fromResource(R.drawable.g_unhealthy_circle)
   }

    private val unHealthyCircle by lazy {   BitmapDescriptorFactory.fromResource(R.drawable.unhealthy_circle)
   }

    private val vUnHealthyCircle by lazy {   BitmapDescriptorFactory.fromResource(R.drawable.v_unhealthy_circle)
   }

    private val hazardCircle by lazy {   BitmapDescriptorFactory.fromResource(R.drawable.hazardous_circle)
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

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGMapsBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        requireActivity().invalidateOptionsMenu()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLocationPermissionsLauncher()
        initQrScannerLauncher()

        watchedLocationsAdapter = WatchedLocationsAdapter(this)
        monitorsAdapter = MonitorsAdapter(this)
        super.setHomeMapOverlay(binding.mapOverlay)


        binding.gMap.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@GMapsFragment)
        }

        viewModel.observeSelectedAqiIndex().observe(
                viewLifecycleOwner, {
            updateSelectedAqiIndex(it)
            observeWatchedLocations()
        }
        )

        requestPermissionsAndShowUserLocation()
    }


    override fun onMapReady(gMap: GoogleMap?) {
        if (gMap != null) {
            mMap = gMap
            mMap?.uiSettings?.apply {
                isCompassEnabled = false
            }
            observeMapRelatedData()

        } else {
            snackBar = binding.container.showSnackBar(
                    isErrorMsg = true,
                    msgResId = R.string.failed_to_load_gmap,
                    actionMessage = R.string.dismiss
            )
        }

    }


    private fun observeWatchedLocations() {
        viewModel.observeWatchedLocations().observe(viewLifecycleOwner, {
            updateWatchedLocations(it)
        })
    }

    private fun observeMapRelatedData() {
        //map data -locations & pm values
        viewModel.observeMapData().observe(
                this, {
            if (it.isNotEmpty()) {
                drawCirclesOnMap(it)
            }
        }
        )
    }

    private fun drawCirclesOnMap(mapDataPoints: List<MapData>) {
        try {
            clearMapCircles()
            mMap?.let {
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
                    val circleMarker = mMap?.addMarker(
                            MarkerOptions().position(mapData.getGMapLocationLatLng())
                                    .icon(mIcon)
                    )
                    circleMarker?.let { marker ->
                        mapCircleMarkers.add(marker)
                    }
                }
            }
        } catch (exc: Exception) {
            myLogger.logThis(tag = LogTags.EXCEPTION, from = "$TAG drawCirclesOnMap()", msg = exc.message, exc = exc)


        }
    }


    private fun clearMapCircles() {
        for (circle in mapCircleMarkers) {
            circle.remove()
        }
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
                val action = GMapsFragmentDirections.actionGMapsFragmentToAddLocation(
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


    /************* life cycle related stuff *********/

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
        binding.gMap.onPause()
        dismissPopUps()
    }


    override fun onResume() {
        super.onResume()
        binding.gMap.onResume()
        viewModel.getMyLocationOrNull()?.let{
            showLocationOnMap(it)
        }
    }

    override fun onStop() {
        super.onStop()
        binding.gMap.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.gMap.onLowMemory()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.gMap.onDestroy()

    }

    /********************* NAVIGATION ***************/
    override fun goToSearchFragment() {
        try {
            val action = GMapsFragmentDirections.actionGMapsFragmentToSearchFragment()
            findNavController().navigate(action)
        } catch (exc: Exception) {
            myLogger.logThis(tag = LogTags.EXCEPTION, from = "$TAG goToSearchFragment()", msg = exc.message, exc = exc)


        }
    }

    override fun onClickWatchedLocation(location: WatchedLocationHighLights) {
        try {
            viewModel.setWatchedLocationInCache(location)
            val action = GMapsFragmentDirections.actionGMapsFragmentToDetailsFragment()
            binding.container.findNavController().navigate(action)
        } catch (exc: Exception) {
            myLogger.logThis(tag = LogTags.EXCEPTION, from = "$TAG  onClickWatchedLocation()", msg = exc.message, exc = exc)


        }
    }

    override fun onClickWatchedMonitor(monitor: MonitorDetails) {
        //todo show history
    }


}