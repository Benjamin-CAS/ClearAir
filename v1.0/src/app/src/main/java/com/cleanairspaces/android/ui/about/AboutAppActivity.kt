package com.cleanairspaces.android.ui.about

import android.os.Bundle
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityAboutAppBinding
import com.cleanairspaces.android.databinding.ActivityLocationDetailsBinding
import com.cleanairspaces.android.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutAppActivity : BaseActivity() {

    private lateinit var binding : ActivityAboutAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)

        binding = ActivityAboutAppBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        //toolbar
        super.setToolBar(binding.toolbarLayout, isHomeAct = false)
    }


}