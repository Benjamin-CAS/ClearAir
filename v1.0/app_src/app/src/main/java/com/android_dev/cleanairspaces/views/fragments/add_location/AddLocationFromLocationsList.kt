package com.android_dev.cleanairspaces.views.fragments.add_location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentAddLocationFromListBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.VerticalSpaceItemDecoration
import com.android_dev.cleanairspaces.views.adapters.FoundLocationsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddLocationFromLocationsList : Fragment(), FoundLocationsAdapter.OnClickItemListener {

    companion object {
        private val TAG = AddLocationFromLocationsList::class.java.simpleName
    }

    @Inject
    lateinit var myLogger: MyLogger


    private var _binding: FragmentAddLocationFromListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddLocationViewModel by activityViewModels()


    private lateinit var foundLocationsAdapter: FoundLocationsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLocationFromListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        initializeSearchView()
        viewModel.observeIndoorLocationsToChooseFrom().observe(viewLifecycleOwner, {
            if (it != null) {
                foundLocationsAdapter.setFoundLocationsList(it)
                if (it.isNotEmpty()) {
                    val title = it[0].name + "\n" + getString(R.string.locations_txt)
                    binding.companyName.text = title
                }
            }
        })
        binding.doneBtn.setOnClickListener {
            viewModel.refreshRecentlyAddedLocationDetails()
            val action =
                AddLocationFromLocationsListDirections.actionAddLocationFromLocationsListToSplashFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        foundLocationsAdapter = FoundLocationsAdapter(this)
        binding.locationSuggestionsRv.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
            )
            adapter = foundLocationsAdapter
            addItemDecoration(VerticalSpaceItemDecoration(30))
        }
    }

    private fun initializeSearchView() {
        binding.searchView.doOnTextChanged { text, _, _, _ ->
            viewModel.searchInIndoorLocations(text.toString())
        }
    }


    override fun onClickFoundLocation(location: WatchedLocationHighLights) {
        viewModel.saveIndoorLocationFromFoundList(location)
        val msg = location.name + " " + getString(R.string.location_added_suffix)
        Toast.makeText(
            requireContext(), msg, Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}