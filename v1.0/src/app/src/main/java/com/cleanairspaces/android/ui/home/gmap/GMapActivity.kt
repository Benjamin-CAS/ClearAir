package com.cleanairspaces.android.ui.home.gmap


import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cleanairspaces.android.databinding.ActivityGmapBinding
import com.cleanairspaces.android.ui.home.BaseMapActivity
import com.cleanairspaces.android.ui.home.MapActionChoices
import com.cleanairspaces.android.ui.home.MapViewModel
import com.cleanairspaces.android.ui.home.adapters.home.MapActionsAdapter
import com.cleanairspaces.android.ui.home.adapters.home.MyLocationsAdapter
import com.cleanairspaces.android.utils.MyLogger
import com.google.android.gms.maps.SupportMapFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GMapActivity : BaseMapActivity() {


    private lateinit var binding: ActivityGmapBinding
    private val viewModel: MapViewModel by viewModels()

    private val TAG = GMapActivity::class.java.simpleName

    override val mapActionsAdapter = MapActionsAdapter(this)
    override val myLocationsAdapter : MyLocationsAdapter by lazy {
        MyLocationsAdapter(this)
    }

    private var mapFragment: SupportMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the content view that renders the map.
        binding = ActivityGmapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        super.setToolBar(binding.toolbarLayout, true)


        /*TODO  Get the SupportMapFragment and request notification when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this) */

        initializeRecyclerViewForUserActions()
    }

    /*************** USER ACTIONS ****************/
    private fun initializeRecyclerViewForUserActions() {
        binding.homeMapOverlay.apply {
            mapActionsRv.layoutManager = LinearLayoutManager(
                this@GMapActivity,
                RecyclerView.HORIZONTAL,
                false
            )
            mapActionsAdapter.setMapActionsList(viewModel.mapActions)
            mapActionsRv.adapter = mapActionsAdapter
        }

    }


    /************ USER ACTIONS ******************/
    override fun onClickAction(actionChoice: MapActionChoices) {
        MyLogger.logThis(TAG, "onClickAction()", "user clicked ${getString(actionChoice.strRes)}")
    }


    /********** MENU *********/
    override fun showUserLocation() {
        //todo
    }

    override fun hideMyLocations() {
        //todo
    }

    override fun showSnackBar(msgRes: Int, isError: Boolean, actionRes: Int?) {
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