package com.cleanairspaces.android.ui.add_locations

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivityAddLocationBinding
import com.cleanairspaces.android.models.entities.SearchSuggestions
import com.cleanairspaces.android.ui.BaseActivity
import com.cleanairspaces.android.ui.add_locations.adapters.SearchSuggestionsAdapter
import com.cleanairspaces.android.utils.VerticalSpaceItemDecoration
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddLocationActivity : BaseActivity(),  SearchSuggestionsAdapter.OnClickItemListener {
    private lateinit var binding: ActivityAddLocationBinding

    private val viewModel : AddLocationActViewModel by viewModels()
    private lateinit var searchSuggestionsAdapter :  SearchSuggestionsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)
        binding = ActivityAddLocationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //toolbar
        super.setToolBar(binding.toolbarLayout, isHomeAct = false)
        setupRecyclerView()
        initializeSearchView()
        viewModel.getSuggestions().observe(this, Observer {
            searchSuggestionsAdapter.setSearchSuggestionsList(it)
        })
    }

    override fun handleBackPress() {
        this@AddLocationActivity.finish()
    }

    private fun setupRecyclerView(){
        searchSuggestionsAdapter =  SearchSuggestionsAdapter(this)
        binding.locationSuggestionsRv.apply {
            layoutManager = LinearLayoutManager(
                this@AddLocationActivity,
                RecyclerView.VERTICAL,
                false
            )
            adapter =  searchSuggestionsAdapter
            addItemDecoration(VerticalSpaceItemDecoration(30))
         }
    }

    private fun initializeSearchView(){
            binding.searchView.doOnTextChanged { text, _, _, _ ->
                viewModel.search(text.toString())
            }
    }

    override fun onClickAction(suggestion: SearchSuggestions) {
        //todo add location
    }

}



