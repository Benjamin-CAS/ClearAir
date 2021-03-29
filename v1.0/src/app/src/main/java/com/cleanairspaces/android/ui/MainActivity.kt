package com.cleanairspaces.android.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.findNavController()
        //specify home fragments
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.aMapFragment,
                R.id.gMapFragment,
                R.id.splashFragment
            )
        )
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setupActionBarWithNavController(navController, appBarConfiguration)

        //hide and show menus depending on fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.aMapFragment,
                R.id.gMapFragment-> {
                    binding.apply {
                        toolbar.isVisible = true
                        toolbarTitle.isVisible = false
                        toolbarLogo.isVisible = true
                        Glide.with(this@MainActivity)
                            .load(R.drawable.clean_air_spaces_logo_name)
                            .into(toolbarLogo)
                    }
                }

                R.id.splashFragment -> {
                    binding.apply {
                        toolbar.isVisible = false
                    }
                }

                R.id.aboutAppFragment -> {
                    binding.apply {
                        toolbar.isVisible = true
                        toolbarLogo.isVisible = false
                        toolbarTitle.isVisible = true
                        toolbarTitle.setText(R.string.map_menu_about_app_txt)
                    }
                }

                else -> {
                    binding.apply {
                        toolbar.isVisible = false
                    }
                }

            }
        }
    }

    //toolbar handle back navigation
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}