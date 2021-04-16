package com.cleanairspaces.android.ui.add_locations

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.ActivitySearchLocationBinding
import com.cleanairspaces.android.models.entities.SearchSuggestions
import com.cleanairspaces.android.ui.BaseActivity
import com.cleanairspaces.android.ui.add_locations.adapters.SearchSuggestionsAdapter
import com.cleanairspaces.android.ui.smart_qr.AddLocationActivity
import com.cleanairspaces.android.utils.VerticalSpaceItemDecoration
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchLocationActivity : BaseActivity(),  SearchSuggestionsAdapter.OnClickItemListener {
    private lateinit var binding: ActivitySearchLocationBinding

    private val viewModel : SearchLocationActViewModel by viewModels()
    private lateinit var searchSuggestionsAdapter :  SearchSuggestionsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_location)
        binding = ActivitySearchLocationBinding.inflate(layoutInflater)
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
        this@SearchLocationActivity.finish()
    }

    private fun setupRecyclerView(){
        searchSuggestionsAdapter =  SearchSuggestionsAdapter(this)
        binding.locationSuggestionsRv.apply {
            layoutManager = LinearLayoutManager(
                this@SearchLocationActivity,
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
        startActivity(
            Intent(this, AddLocationActivity::class.java).putExtra(
                AddLocationActivity.INTENT_EXTRA_SEARCHED_TAG, suggestion
            ))
       finish()
    }

}



