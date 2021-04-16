package com.cleanairspaces.android.ui.home.gmap


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.cleanairspaces.android.databinding.ActivityGmapBinding
import com.cleanairspaces.android.ui.home.BaseMapActivity
import com.cleanairspaces.android.ui.home.adapters.MapActionsAdapter
import com.cleanairspaces.android.ui.home.adapters.MyLocationsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GMapActivity : BaseMapActivity() {


    private lateinit var binding: ActivityGmapBinding

    private val TAG = GMapActivity::class.java.simpleName

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
        binding = ActivityGmapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initPermissionsLauncher()
        initQrScannerLauncher()

        super.setToolBar(binding.toolbarLayout, true)
        super.homeMapOverlay = binding.homeMapOverlay

        initializeMap(savedInstanceState)

        super.initializeRecyclerViewForUserActions()
        super.initializeMyLocationsRecycler()
    }

    private fun initializeMap(savedInstanceState: Bundle?) {

    }

    /********** MENU *********/
    override fun showUserLocation() {
        //todo
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