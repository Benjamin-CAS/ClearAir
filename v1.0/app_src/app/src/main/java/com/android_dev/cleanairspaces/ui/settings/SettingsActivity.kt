package com.android_dev.cleanairspaces.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.android_dev.cleanairspaces.BaseActivity
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.SettingsActivityBinding
import com.android_dev.cleanairspaces.ui.welcome.SplashActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

    private var displayedMapsSettings: Boolean = false
    private var displayedMapLangSettings: Boolean = false
    private var displayedAqiSettings: Boolean = false

    private var mapsSelector: AutoCompleteTextView? = null
    private var mapLangSelector: AutoCompleteTextView? = null
    private var aqiTypeSelector: AutoCompleteTextView? = null

    private lateinit var mapsAdapter: ArrayAdapter<String>
    private lateinit var mapLangAdapter: ArrayAdapter<String>
    private lateinit var aqiIndexesAdapter: ArrayAdapter<String>

    private lateinit var mapsArr: Array<String>
    private lateinit var mapLangArr: Array<String>
    private lateinit var aqiIndexArr: Array<String>

    private lateinit var binding: SettingsActivityBinding

    private val viewModel: SettingsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        super.setToolBar(binding.topBar, isHomeAct = false)

        initViews()
        observeSettings()
    }

    private fun initViews() {
        aqiIndexArr = resources.getStringArray(R.array.aqi_indexes)
        aqiIndexesAdapter =
            ArrayAdapter(this@SettingsActivity, R.layout.settings_drop_down_item, aqiIndexArr)
        mapLangArr = resources.getStringArray(R.array.map_languages)
        mapLangAdapter =
            ArrayAdapter(this@SettingsActivity, R.layout.settings_drop_down_item, mapLangArr)
        mapsArr = resources.getStringArray(R.array.maps)
        mapsAdapter = ArrayAdapter(this@SettingsActivity, R.layout.settings_drop_down_item, mapsArr)
        setupPMAdapter()
        setupMapsAdapter()
        setupMapLanguageAdapter()
    }

    private fun setupPMAdapter() {
        aqiTypeSelector = (binding.aqiSelect.editText as? AutoCompleteTextView)
        aqiTypeSelector?.setAdapter(aqiIndexesAdapter)
        aqiTypeSelector?.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedAqi: String =
                    aqiIndexesAdapter.getItem(position)
                        ?: getString(R.string.default_aqi_pm_2_5)
                viewModel.setAQIIndex(selectedAqi)
            }
    }

    private fun setupMapLanguageAdapter() {
        mapLangSelector = (binding.mapLanguageSelect.editText as? AutoCompleteTextView)
        mapLangSelector?.setAdapter(mapLangAdapter)
        mapLangSelector?.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedMapLang: String =
                    mapLangAdapter.getItem(position)
                        ?: getString(R.string.map_lang_chinese)
                viewModel.setSelectedMapLang(selectedMapLang)
            }
    }

    private fun setupMapsAdapter() {
        mapsSelector = (binding.mapSelect.editText as? AutoCompleteTextView)
        mapsSelector?.setAdapter(mapsAdapter)
        mapsSelector?.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedMap: String =
                    mapsAdapter.getItem(position)
                        ?: getString(R.string.default_map_a_map)
                viewModel.setSelectedMap(selectedMap)
            }
    }

    override fun handleBackPress() {
        finish()
    }

    private fun observeSettings() {
        viewModel.observeAQIIndex().observe(this, Observer { selectedAqi ->
            if (displayedAqiSettings) return@Observer
            val defaultPM = aqiIndexesAdapter.getItem(0)
            if (selectedAqi.isNullOrBlank()) {
                aqiTypeSelector?.setText(defaultPM, false)
            } else {
                val index = aqiIndexesAdapter.getPosition(selectedAqi)
                val indexTxt = if (index != -1) selectedAqi else defaultPM
                aqiTypeSelector?.setText(indexTxt, false)
            }
            displayedAqiSettings = true
        })

        viewModel.observeSelectedMapLang().observe(this, Observer { selectedMapLang ->
            if (displayedMapLangSettings) return@Observer
            val defaultMapLang = mapLangAdapter.getItem(0)
            if (selectedMapLang.isNullOrBlank()) {
                mapLangSelector?.setText(defaultMapLang, false)
            } else {
                val index = mapLangAdapter.getPosition(selectedMapLang)
                val indexTxt = if (index != -1) selectedMapLang else defaultMapLang
                mapLangSelector?.setText(indexTxt, false)
            }
            displayedMapLangSettings = true

        })

        viewModel.observeSelectedMap().observe(this, Observer { selectedMap ->
            if (displayedMapsSettings) return@Observer
            val defaultMap = mapsAdapter.getItem(0)
            if (selectedMap.isNullOrBlank()) {
                mapsSelector?.setText(defaultMap, false)
            } else {
                val index = mapsAdapter.getPosition(selectedMap)
                val indexTxt = if (index != -1) selectedMap else defaultMap
                mapsSelector?.setText(indexTxt, false)
            }
            displayedMapsSettings = true

        })
    }

    private fun reloadApp() {
        finishAffinity()
        startActivity(Intent(this, SplashActivity::class.java))
    }


    /****************** MENU **************/
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.settings_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                reloadApp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}