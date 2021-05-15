package com.android_dev.cleanairspaces.views

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
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
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainAct : AppCompatActivity() {

    companion object {
        private val TAG = MainAct::class.java.simpleName
    }

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
                R.id.addLocationFromLocationsList,
                R.id.monitorHistoryFragment-> {
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
                R.id.historyFragment,
                R.id.monitorsFragment -> {
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

        //log
        lifecycleScope.launch(Dispatchers.IO) {
            myLogger.logThis(
                    LogTags.USER_ACTION_OPEN_APP, TAG
            )
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

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch(Dispatchers.IO) {
            myLogger.logThis(
                    LogTags.USER_ACTION_CLOSE_APP, TAG
            )
        }
    }

}