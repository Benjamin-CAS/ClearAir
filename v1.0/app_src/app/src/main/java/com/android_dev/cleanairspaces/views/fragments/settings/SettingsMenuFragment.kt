package com.android_dev.cleanairspaces.views.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentSettingsMenuBinding
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsMenuFragment : Fragment() {

    companion object {
        private val TAG = SettingsMenuFragment::class.java.simpleName
    }

    private var displayedMapsSettings: Boolean = false
    private var displayedMapLangSettings: Boolean = false
    private var displayedAqiSettings: Boolean = false

    @Inject
    lateinit var myLogger: MyLogger

    private var mapsSelector: AutoCompleteTextView? = null
    private var mapLangSelector: AutoCompleteTextView? = null
    private var aqiTypeSelector: AutoCompleteTextView? = null

    private lateinit var mapsAdapter: ArrayAdapter<String>
    private lateinit var mapLangAdapter: ArrayAdapter<String>
    private lateinit var aqiIndexesAdapter: ArrayAdapter<String>

    private lateinit var mapsArr: Array<String>
    private lateinit var mapLangArr: Array<String>
    private lateinit var aqiIndexArr: Array<String>


    private var _binding: FragmentSettingsMenuBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsMenuBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        requireActivity().invalidateOptionsMenu()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        observeSettings()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    reloadApp()
                }

            })
    }

    private fun reloadApp() {
        val action = SettingsMenuFragmentDirections.actionSettingsMenuFragmentToSplashFragment()
        findNavController().navigate(action)
    }

    private fun initViews() {
        aqiIndexArr = resources.getStringArray(R.array.aqi_indexes)
        aqiIndexesAdapter =
            ArrayAdapter(requireContext(), R.layout.settings_drop_down_item, aqiIndexArr)
        mapLangArr = resources.getStringArray(R.array.map_languages)
        mapLangAdapter =
            ArrayAdapter(requireContext(), R.layout.settings_drop_down_item, mapLangArr)
        mapsArr = resources.getStringArray(R.array.maps)
        mapsAdapter = ArrayAdapter(requireContext(), R.layout.settings_drop_down_item, mapsArr)
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




    private fun observeSettings() {
        viewModel.observeAQIIndex().observe(viewLifecycleOwner, { selectedAqi ->
            if (!displayedAqiSettings) {
                val defaultPM = aqiIndexesAdapter.getItem(0)
                if (selectedAqi.isNullOrBlank()) {
                    aqiTypeSelector?.setText(defaultPM, false)
                } else {
                    val index = aqiIndexesAdapter.getPosition(selectedAqi)
                    val indexTxt = if (index != -1) selectedAqi else defaultPM
                    aqiTypeSelector?.setText(indexTxt, false)
                }
                displayedAqiSettings = true
            }
        })

        viewModel.observeSelectedMapLang().observe(viewLifecycleOwner, { selectedMapLang ->
            if (!displayedMapLangSettings) {
                val defaultMapLang = mapLangAdapter.getItem(0)
                if (selectedMapLang.isNullOrBlank()) {
                    mapLangSelector?.setText(defaultMapLang, false)
                } else {
                    val index = mapLangAdapter.getPosition(selectedMapLang)
                    val indexTxt = if (index != -1) selectedMapLang else defaultMapLang
                    mapLangSelector?.setText(indexTxt, false)
                }
                displayedMapLangSettings = true
            }

        })

        viewModel.observeSelectedMap().observe(viewLifecycleOwner, { selectedMap ->
            if (!displayedMapsSettings) {
                val defaultMap = mapsAdapter.getItem(0)
                if (selectedMap.isNullOrBlank()) {
                    mapsSelector?.setText(defaultMap, false)
                } else {
                    val index = mapsAdapter.getPosition(selectedMap)
                    val indexTxt = if (index != -1) selectedMap else defaultMap
                    mapsSelector?.setText(indexTxt, false)
                }
                displayedMapsSettings = true
            }

        })
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.splashFragment).isVisible = true
        menu.findItem(R.id.settingsMenuFragment).isVisible = false
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}