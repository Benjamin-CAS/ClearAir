package com.cleanairspaces.android.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cleanairspaces.android.R
import com.cleanairspaces.android.ui.home.adapters.MapActionsAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
open class BaseMapActivity : AppCompatActivity(), MapActionsAdapter.ClickListener {


    var popUp: AlertDialog? = null
    var snackbar: Snackbar? = null
    
    //prepare bitmaps
    val aQIGoodBitmap = R.drawable.good_circle
    val aQIModerateBitmap = R.drawable.moderate_circle
    val aQIGUnhealthyBitmap = R.drawable.g_unhealthy_circle
    val aQIUnhealthyBitmap = R.drawable.unhealthy_circle
    val aQIVUnhealthyBitmap = R.drawable.v_unhealthy_circle
    val aQIHazardousBitmap = R.drawable.hazardous_circle
    val aQIBeyondBitmap = R.drawable.beyond_circle
    val aQICNExcellentBitmap = R.drawable.excellent


    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    val mapActionsAdapter = MapActionsAdapter(this)

    //TO BE IMPLEMENTED
     open fun showUserLocation(){}
     open fun hideMyLocations(){}

    /********* PERMISSIONS ***************/
    fun requestPermissionsToShowUserLocation() {
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

    fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    /*********** USER ACTIONS *******/
    override fun onClickAction(actionChoice: MapActionChoices) {
        when(actionChoice){
            MapActionChoices.SMART_QR -> {
                scanQRCode()
            }
            MapActionChoices.MAP_VIEW -> {
                hideMyLocations()
            }
            MapActionChoices.ADD ->  {
                //todo
            }
        }
    }

    /************ QR CODE ***********/
    private fun scanQRCode(){

    }

    /***************** DIALOGS ****************/
    fun promptMyLocationSettings() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager?
        if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //showTurnOnGPSDialog
            showDialog(msgRes = R.string.turn_on_gps_prompt, positiveAction = { startActivity(
                Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            ) })
        }
    }

    fun dismissPopUps() {
        popUp?.let {
            if (it.isShowing) it.dismiss()
        }
        snackbar?.let {
            if (it.isShown) it.dismiss()
        }
        popUp = null
        snackbar = null
    }
    
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

    open fun showSnackBar(
        msgRes: Int,
        isError: Boolean = false,
        actionRes: Int? = null
    ) {}

    /****************** MENU **************/
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

}