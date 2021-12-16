package com.android_dev.cleanairspaces.views.fragments.amaps

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentAMapsBinding
import com.android_dev.cleanairspaces.persistence.api.mqtt.CasMqttClient
import com.android_dev.cleanairspaces.persistence.api.mqtt.DeviceUpdateMqttMessage
import com.android_dev.cleanairspaces.persistence.local.models.entities.*
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MY_LOCATION_ZOOM_LEVEL
import com.android_dev.cleanairspaces.utils.getAQIStatusFromPM25
import com.android_dev.cleanairspaces.views.adapters.WatchedLocationsAndDevicesAdapter
import com.android_dev.cleanairspaces.views.fragments.maps_overlay.BaseMapFragment
import com.android_dev.cleanairspaces.views.fragments.maps_overlay.MapsViewModel.Companion.mapViewName
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AMapsFragment : BaseMapFragment() {
    companion object {
        const val TAG = "AMapsFragment"
        const val MAP_VIEW = "MAP_VIEW"
    }
    private var managerBtnClickLiveData = true
    private val goodCircle: BitmapDescriptor by lazy {
        BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.good_circle)   // 好的
        )
    }
    private val moderateCircle: BitmapDescriptor by lazy {
        BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.moderate_circle)   // 适宜的
        )
    }
    private val gUnhealthyCircle: BitmapDescriptor by lazy {
        BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.g_unhealthy_circle)   // 不良的
        )
    }
    private val unHealthyCircle: BitmapDescriptor by lazy {
        BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.unhealthy_circle)
        )
    }
    private val vUnHealthyCircle: BitmapDescriptor by lazy {
        BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.v_unhealthy_circle)
        )
    }
    private val hazardCircle: BitmapDescriptor by lazy {
        BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.hazardous_circle)  // 危险的
        )
    }


    private var _binding: FragmentAMapsBinding? = null
    private val binding get() = _binding!!


    private val mapCirclesMarkers: ArrayList<MarkerMapTypeWrapper> = arrayListOf()  // 地图圆环标记
    private var mapView: MapView? = null
    private var aMap: AMap? = null


    /**
     * Initialize launchers for requesting permissions   初始化启动器请求权限
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
        setHasOptionsMenu(true)
        requireActivity().invalidateOptionsMenu()
        viewModel.mapHasBeenInitialized.value = false

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goHome()
                }

            })
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLocationPermissionsLauncher()
        initQrScannerLauncher()
        watchedItemsAdapter = WatchedLocationsAndDevicesAdapter(this, displayFav = false, this,this)
        super.setHomeMapOverlay(binding.mapOverlay)
        //user setting - language
        viewModel.mapHasBeenInitialized.value =
            initializeMap(savedInstanceState = savedInstanceState)
        observeMapRelatedData()
        requestPermissionsAndShowUserLocation()

        viewModel.mapHasBeenInitialized.observe(viewLifecycleOwner, {
            if (it) {
                initializeDataAfterMapIsReady()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeDataAfterMapIsReady() {
        Log.e(TAG, "initializeDataAfterMapIsReady: 地图加载完毕")
        // 设置语言
        viewModel.observeMapLang().observe(viewLifecycleOwner, {
            setMapLang(mapLangSet = it)
        })
        // 设置空气指数PM2.5
        viewModel.observeAqiIndex().observe(viewLifecycleOwner, {
            viewModel.aqiIndex = it
            updateIndexForWatchedLocations(it)
        })
        observeWatchedLocations()
        observeDevicesIWatch()
        observeAirConditionersIWatch()
        viewModel.getMqttMessage().observe(
            viewLifecycleOwner, {
                it?.let { newMsg ->
                    connectAndPublish(newMsg)
                }
            }
        )
    }

    private fun observeDevicesIWatch() {
        viewModel.observeDevicesIWatch().observe(viewLifecycleOwner, {
            it?.let { devices ->
                Log.e(TAG, "observeDevicesIWatch: $devices")
                updateWatchedDevices(devices)
            }
        })
    }

    private fun observeAirConditionersIWatch() {
        viewModel.observeAirConditionerIWatch().observe(viewLifecycleOwner) {
            it?.let {
                updateWatchedAirConditioner(it)
            }
        }
    }

    private fun observeWatchedLocations() {
        viewModel.observeWatchedLocations().observe(viewLifecycleOwner, {
            updateWatchedLocations(it)
        })
    }

    private fun initializeMap(savedInstanceState: Bundle?): Boolean {
        binding.apply {
            mapView = map
            mapView?.let {
                aMap = it.map
                it.onCreate(savedInstanceState)
                aMap?.apply {
                    uiSettings?.isMyLocationButtonEnabled = false
                    isMyLocationEnabled = false
                    uiSettings.isZoomControlsEnabled = false
                    return true
                }
                return false
            }
            return false
        }
    }

    private fun setMapLang(mapLangSet: String?) {
        aMap?.apply {
            if (mapLangSet == null || mapLangSet == getString(R.string.map_lang_chinese)) {
                setMapLanguage(AMap.CHINESE)
            } else {
                setMapLanguage(AMap.ENGLISH)
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
        val currentUserLocation = LatLng(location.latitude, location.longitude)  // 当前坐标
        aMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(currentUserLocation, MY_LOCATION_ZOOM_LEVEL)
        )
        viewModel.myLocMarkerOnAMap?.remove()
        val mIcon = BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.ic_my_location_marker)
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
        viewModel.myLocMarkerOnAMap = aMap?.addMarker(markerOptions)   // 将自己的坐标添加在地图上
    }

    // 在地图上绘制圆
    private fun drawCirclesOnMap(mapDataPoints: List<MapData>) {
        try {
            clearMapCircles(mapType = mapDataPoints[0].type)
            aMap?.let {
                for (mapData in mapDataPoints) {
                    //use default aqi index --todo --use settings? re draws index
                    val aqiStatus = getAQIStatusFromPM25(mapData.pm25, aqiIndex = null)
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
                        icon(mIcon)
                    }
                    val circleMarker = aMap?.addMarker(markerOptions)
                    circleMarker?.let { marker ->
                        mapCirclesMarkers.add(
                            MarkerMapTypeWrapper(
                                marker = marker,
                                mapType = mapData.type
                            )
                        )
                    }
                }
            }
        } catch (exc: Exception) {
            lifecycleScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG drawCirclesOnMap()",
                    msg = exc.message,
                    exc = exc
                )
            }
        }
    }


    private fun clearMapCircles(mapType: MapDataType) {
        val circlesToClear = mapCirclesMarkers.filter { it.mapType == mapType }
        for (circle in circlesToClear) {
            circle.marker.remove()
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
                Log.e(TAG, "handleScannedQrIntent: $qrContent")
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
        viewModel.getMyLocationOrNull()?.let {
            Log.e(TAG, "onResume: $it")
            showLocationOnMap(it)
        }
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
            lifecycleScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG goToSearchFragment()",
                    msg = exc.message,
                    exc = exc
                )
            }
        }
    }

    override fun onClickWatchedLocation(locationHighLights: WatchedLocationHighLights) {
        Log.e(TAG, "onClickWatchedLocation: 根点击了")
        try {
            viewModel.setWatchedLocationInCache(locationHighLights, viewModel.aqiIndex)
            mapViewName = "AMAP"
            binding.container.findNavController().navigate(R.id.detailsFragment)
        } catch (exc: Exception) {
            lifecycleScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG onClickWatchedLocation",
                    msg = exc.message,
                    exc = exc
                )
            }
        }
    }

    override fun onSwipeToDeleteAirConditioner(airConditionerEntity: AirConditionerEntity) {
        viewModel.stopWatchingAirConditioner(airConditionerEntity)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun submitAirConditioner(locId: String, address: String, param: String) {
        casMqttClient.connectAndPublish(
            DeviceUpdateMqttMessage(
                address,param
            )
        ){

        }
        lifecycleScope.launch {
            delay(7000)
            viewModel.refreshAirConditioner(locId)
        }
    }

    override fun cancelAirConditioner(locId: String) {
        viewModel.refreshAirConditioner(locId)
    }

    override fun managerBtnClickListener(airConditionerEntity: AirConditionerEntity) = if (managerBtnClickLiveData){
        managerBtnClickLiveData = false
        managerBtnClickLiveData
    }else {
        managerBtnClickLiveData = true
        managerBtnClickLiveData
    }

    /************************* WATCHED DEVICES *************************/
    override fun onClickToggleWatchDevice(device: DevicesDetails) {
        viewModel.watchThisDevice(device, watchDevice = !device.watch_device)
    }

    override fun onClickToggleWatchAirConditioner(airConditionerEntity: AirConditionerEntity) {
        viewModel.watchThisAirConditioner(
            airConditionerEntity,
            !airConditionerEntity.watchAirConditioner
        )
    }

    override fun onSwipeToDeleteDevice(device: DevicesDetails) {
        viewModel.stopWatchingDevice(device)
    }

    override fun onToggleFreshAir(device: DevicesDetails, status: String) {
        viewModel.onToggleFreshAir(device, status)
    }

    override fun onToggleFanSpeed(device: DevicesDetails, status: String, speed: String?) {
        viewModel.onToggleFanSpeed(device, status, speed)
    }

    override fun onToggleMode(device: DevicesDetails, toMode: String) {
        viewModel.onToggleMode(device, toMode)
    }

    override fun onToggleDuctFit(device: DevicesDetails, status: String) {
        viewModel.onToggleDuctFit(device, status)
    }


    /************ MQTT **************/
    @Inject
    lateinit var casMqttClient: CasMqttClient

    @RequiresApi(Build.VERSION_CODES.N)
    private fun connectAndPublish(deviceUpdateMqttMessage: DeviceUpdateMqttMessage) {
        casMqttClient.connectAndPublish(deviceUpdateMqttMessage){

        }
        viewModel.setMqttStatus(null) //clear
        viewModel.refreshDevicesAfterDelay()
    }
}

data class MarkerMapTypeWrapper(
    val marker: Marker,
    val mapType: MapDataType
)