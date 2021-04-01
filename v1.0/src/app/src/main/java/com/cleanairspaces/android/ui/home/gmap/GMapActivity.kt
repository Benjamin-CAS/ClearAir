package com.cleanairspaces.android.ui.home.gmap


import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityGmapBinding
import com.cleanairspaces.android.ui.home.MapActionChoices
import com.cleanairspaces.android.ui.home.MapViewModel
import com.cleanairspaces.android.ui.home.adapters.MapActionsAdapter
import com.cleanairspaces.android.utils.MyLogger
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.snackbar.Snackbar

@AndroidEntryPoint
class GMapActivity : AppCompatActivity(), MapActionsAdapter.ClickListener  {


    private val mapActionsAdapter = MapActionsAdapter(this)

    private lateinit var binding: ActivityGmapBinding
    private val viewModel : MapViewModel by viewModels()

    private var popUp: AlertDialog? = null
    private var snackbar: Snackbar? = null

    private val TAG = GMapActivity::class.java.simpleName
    private var mapFragment: SupportMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the content view that renders the map.
        binding = ActivityGmapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.apply {
            Glide.with(this@GMapActivity)
                .load(R.drawable.clean_air_spaces_logo_name)
                .into(toolbarLogo)
        }

        /*TODO  Get the SupportMapFragment and request notification when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this) */

        initializeRecyclerViewForUserActions()
    }

    /*************** USER ACTIONS ****************/
    private fun initializeRecyclerViewForUserActions(){
        binding.apply {
            mapActionsRv.layoutManager = LinearLayoutManager(
                this@GMapActivity,
                RecyclerView.HORIZONTAL,
                false
            )
            mapActionsAdapter.setMapActionsList(viewModel.mapActions)
            mapActionsRv.adapter = mapActionsAdapter
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


    /************ USER ACTIONS ******************/
    override fun onClickAction(actionChoice: MapActionChoices) {
        MyLogger.logThis(TAG, "onClickAction()", "user clicked ${getString(actionChoice.strRes)}")
    }
}