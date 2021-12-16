package com.android_dev.cleanairspaces.views

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.lifecycle.asLiveData
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
import com.android_dev.cleanairspaces.persistence.local.DataStoreManager
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.views.fragments.settings.SettingsMenuFragment
import com.tencent.mmkv.MMKV
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs


@AndroidEntryPoint
class MainAct : AppCompatActivity() {
    private val mk: MMKV = MMKV.defaultMMKV()
    companion object {
        private val TAG = MainAct::class.java.simpleName
        private const val LANGUAGE = "LANGUAGE"
        private const val COUNTRY = "COUNTRY"
    }
    @Inject
    lateinit var myLogger: MyLogger
    @Inject
    lateinit var dataStoreManager: DataStoreManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window,false)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarLayout.toolbar) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val height = abs(statusBar.top - statusBar.bottom)
            v.updatePadding(top = height)
            insets
        }

//        val c =WindowCompat.getInsetsController(window,binding.root)
//        c?.isAppearanceLightStatusBars =false
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

                R.id.aboutAppFragment,
                R.id.webViewFragment,
                R.id.addLocation,
                R.id.settingsMenuFragment,
                R.id.searchFragment,
                R.id.addLocationFromLocationsList,
                R.id.monitorHistoryFragment -> {
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
                R.id.monitorsFragment,
                R.id.devicesFragment -> {
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
        Log.e(TAG, "onOptionsItemSelected: 点击了点击了")
        Log.e(TAG, "onOptionsItemSelected:${item.title}")
        lifecycleScope.launch {
            if (item.itemId == R.id.splashFragment){
                val settingLanguage = SettingsMenuFragment.language
                if (settingLanguage.isNotBlank()){
                    when(settingLanguage){
                        "English" -> {
                            mk.encode(LANGUAGE,"en")
                            mk.encode(COUNTRY,"US")
                        }
                        "Chinese" -> {
                            mk.encode(LANGUAGE,"zh")
                            mk.encode(COUNTRY,"CN")
                        }
                        "Spain" -> {
                            mk.encode(LANGUAGE,"es")
                            mk.encode(COUNTRY,"ES")
                        }
                    }
                    recreate()
                }
            }
        }
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

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.createConfigurationContext(Configuration(newBase.resources.configuration).apply {
            setLocale(Locale(mk.decodeString(LANGUAGE)?:"",mk.decodeString(COUNTRY)?:""))
        }))
    }
}