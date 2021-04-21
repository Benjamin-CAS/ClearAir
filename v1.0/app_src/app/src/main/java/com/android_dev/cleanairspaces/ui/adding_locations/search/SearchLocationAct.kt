package com.android_dev.cleanairspaces.ui.adding_locations.search

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.BaseActivity
import com.android_dev.cleanairspaces.databinding.ActivitySearchLocationBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.ui.adding_locations.add.AddLocationActivity
import com.android_dev.cleanairspaces.ui.adding_locations.search.adapters.SearchSuggestionsAdapter
import com.android_dev.cleanairspaces.utils.VerticalSpaceItemDecoration
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchLocationAct : BaseActivity(), SearchSuggestionsAdapter.OnClickItemListener {
    companion object {
        private val TAG = SearchLocationAct::class.java.simpleName
    }

    private lateinit var binding: ActivitySearchLocationBinding

    private val viewModel: SearchLocationViewModel by viewModels()

    private lateinit var searchSuggestionsAdapter: SearchSuggestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchLocationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        super.setToolBar(binding.topToolbar, isHomeAct = false)

        setupRecyclerView()
        initializeSearchView()
        viewModel.getSuggestions().observe(this, Observer {
            searchSuggestionsAdapter.setSearchSuggestionsList(it)
        })
    }

    private fun setupRecyclerView() {
        searchSuggestionsAdapter = SearchSuggestionsAdapter(this)
        binding.locationSuggestionsRv.apply {
            layoutManager = LinearLayoutManager(
                this@SearchLocationAct,
                RecyclerView.VERTICAL,
                false
            )
            adapter = searchSuggestionsAdapter
            addItemDecoration(VerticalSpaceItemDecoration(30))
        }
    }

    private fun initializeSearchView() {
        binding.searchView.doOnTextChanged { text, _, _, _ ->
            viewModel.search(text.toString())
        }
    }


    override fun onClickSearchSuggestion(suggestion: SearchSuggestionsData) {
        val tag = when {
            suggestion.isForIndoorLoc -> AddLocationActivity.INTENT_FROM_SEARCHED_INDOOR_LOC
            suggestion.isForOutDoorLoc -> AddLocationActivity.INTENT_FROM_SEARCHED_OUTDOOR_LOC
            else -> AddLocationActivity.INTENT_FROM_SEARCHED_MONITOR_LOC
        }
        startActivity(
            Intent(this, AddLocationActivity::class.java).putExtra(
                tag, suggestion
            )
        )
        finish()
    }


    override fun handleBackPress() {
        super.handleBackPress()
        finish()
    }


}