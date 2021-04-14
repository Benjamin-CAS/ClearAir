package com.cleanairspaces.android.ui.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivitySettingsBinding
import com.cleanairspaces.android.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private val TAG = SettingsActivity::class.java.simpleName
    private val viewModel: SettingsActivityViewModel by viewModels()
    private var aqiTypeSelector: AutoCompleteTextView? = null


    private lateinit var aqiIndexesAdapter: ArrayAdapter<String>
    private lateinit var aqiIndexes: Array<String>
    private var displayedSavedSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        super.setToolBar(binding.toolbarLayout, false)

        aqiIndexes = resources.getStringArray(R.array.aqi_indexes)
        aqiIndexesAdapter = ArrayAdapter(this@SettingsActivity, R.layout.aqi_index_item, aqiIndexes)
        binding.progressCircular.isVisible = true
        setupPMAdapter()
        observeSettings()
    }

    private fun observeSettings() {
        viewModel.getSelectedAqi().observe(this, Observer { selectedAqi ->
            if (displayedSavedSettings) return@Observer
            binding.progressCircular.isVisible = false
            val defaultPM = aqiIndexesAdapter.getItem(0)
            if (selectedAqi.isNullOrBlank()) {
                aqiTypeSelector?.setText(defaultPM, false)
            } else {
                val index = aqiIndexesAdapter.getPosition(selectedAqi)
                val indexTxt = if (index != -1) selectedAqi else defaultPM
                aqiTypeSelector?.setText(indexTxt, false)
            }
            displayedSavedSettings = true
        })
    }

    private fun setupPMAdapter() {
        aqiTypeSelector = (binding.aqiSelect.editText as? AutoCompleteTextView)
        aqiTypeSelector?.setAdapter(aqiIndexesAdapter)
        aqiTypeSelector?.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedAqi: String =
                    aqiIndexesAdapter.getItem(position)
                        ?: getString(R.string.default_pm_index_value)
                viewModel.setSelectedAqi(selectedAqi)
            }
    }


    override fun handleBackPress() {
        this.finish()
    }


    private fun saveChanges() {
        this.finish()
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
                saveChanges()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}