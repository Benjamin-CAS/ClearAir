package com.android_dev.cleanairspaces

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android_dev.cleanairspaces.databinding.ToolbarLayoutBinding
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

    open fun handleBackPress() {}
    var snackBar: Snackbar? = null
    var popUp: AlertDialog? = null

    fun setToolBar(toolbarLayout: ToolbarLayoutBinding, isHomeAct: Boolean) {
        setSupportActionBar(toolbarLayout.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarLayout.apply {
            Glide.with(toolbarLayout.toolbar.context)
                .load(R.drawable.clean_air_spaces_logo_name)
                .into(toolbarLogo)
            if (!isHomeAct) {
                toolbar.setNavigationIcon(R.drawable.ic_back)
                toolbar.setNavigationOnClickListener(
                    View.OnClickListener {
                        handleBackPress()
                    }
                )
            }
        }
    }


    fun showCustomDialog(msgRes: Int, okRes: Int, dismissRes: Int, positiveAction: () -> Unit) {
        popUp?.let {
            if (it.isShowing) it.dismiss()
        }
        popUp = MaterialAlertDialogBuilder(this)
            .setTitle(msgRes)
            .setPositiveButton(
                okRes
            ) { dialog, _ ->
                positiveAction.invoke()
                dialog.dismiss()
            }
            .setNeutralButton(
                dismissRes
            ) { dialog, _ ->
                dialog.dismiss()
            }.create()

        popUp?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        snackBar?.let {
            if (it.isShown)
                it.dismiss()
        }
        popUp?.let {
            if (it.isShowing) it.dismiss()
        }
        snackBar = null
        popUp = null
    }

}