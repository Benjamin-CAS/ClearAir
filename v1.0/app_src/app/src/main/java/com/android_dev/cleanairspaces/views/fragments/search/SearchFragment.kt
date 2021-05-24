package com.android_dev.cleanairspaces.views.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.databinding.FragmentSearchBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.VerticalSpaceItemDecoration
import com.android_dev.cleanairspaces.views.adapters.SearchSuggestionsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(), SearchSuggestionsAdapter.OnClickItemListener {

    companion object {
        private val TAG = SearchFragment::class.java.simpleName
    }

    @Inject
    lateinit var myLogger: MyLogger


    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchLocationViewModel by viewModels()

    private lateinit var searchSuggestionsAdapter: SearchSuggestionsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        initializeSearchView()
        viewModel.getSuggestions().observe(viewLifecycleOwner, {
            searchSuggestionsAdapter.setSearchSuggestionsList(it)
        })
    }

    private fun setupRecyclerView() {
        searchSuggestionsAdapter = SearchSuggestionsAdapter(this)
        binding.locationSuggestionsRv.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
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
        try {
            val query = binding.searchView.text.toString()
            val msg = "searched $query clicked ${suggestion.nameToDisplay}"
            lifecycleScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                    tag = LogTags.USER_ACTION_SEARCH,
                    from = TAG,
                    msg = msg
                )
            }
            val action =
                when {
                    suggestion.isForIndoorLoc -> SearchFragmentDirections.actionSearchFragmentToAddLocation(
                        locDataIsIndoorQuery = suggestion
                    )
                    suggestion.isForOutDoorLoc -> SearchFragmentDirections.actionSearchFragmentToAddLocation(
                        locDataIsOutdoorQuery = suggestion
                    )
                    else -> null
                }
            action?.let { navDirection ->
                findNavController().navigate(navDirection)
            }

        } catch (exc: Exception) {
            lifecycleScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = TAG,
                    msg = exc.message,
                    exc = exc
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}