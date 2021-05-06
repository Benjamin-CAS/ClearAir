package com.android_dev.cleanairspaces.views

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.ActivityMainBinding
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainAct : AppCompatActivity() {

    @Inject
    lateinit var myLogger: MyLogger


    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        //specify home fragments
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.splashFragment,
                R.id.AMapsFragment,
                R.id.GMapsFragment
            )
        )
        setSupportActionBar(binding.toolbarLayout.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)


        //hide and show menus depending on fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.splashFragment -> {
                    binding.apply {
                        toolbarLayout.toolbar.isVisible = false
                        bottomNav.isVisible = false
                    }
                }

                R.id.AMapsFragment,
                R.id.GMapsFragment -> {
                    binding.apply {
                        toolbarLayout.toolbar.isVisible = true
                        bottomNav.isVisible = false
                    }
                }

                R.id.addLocation,
                R.id.settingsMenuFragment,
                R.id.searchFragment,
                R.id.addLocationFromLocationsList -> {
                    binding.apply {
                        toolbarLayout.apply {
                            toolbar.isVisible = true
                            toolbar.setNavigationIcon(R.drawable.ic_back)
                            toolbar.setNavigationOnClickListener {
                                navController.navigateUp()
                            }
                        }
                        bottomNav.isVisible = false
                    }
                }

                R.id.detailsFragment,
                R.id.historyFragment -> {
                    binding.apply {
                        toolbarLayout.apply {
                            toolbar.isVisible = true
                            toolbar.setNavigationIcon(R.drawable.ic_back)
                            toolbar.setNavigationOnClickListener {
                                navController.navigateUp()
                            }
                        }
                        bottomNav.isVisible = true
                    }
                }

                else -> {
                    binding.apply {
                        toolbarLayout.toolbar.isVisible = false
                        bottomNav.isVisible = false
                    }
                }

            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }


    //toolbar handle back navigation
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}