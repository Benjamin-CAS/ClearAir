package com.cleanairspaces.android.ui

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ToolbarLayoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

    open fun handleBackPress() {}

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


}