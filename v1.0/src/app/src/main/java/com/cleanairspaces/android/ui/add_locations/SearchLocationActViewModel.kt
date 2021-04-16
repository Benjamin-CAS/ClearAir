package com.cleanairspaces.android.ui.add_locations

import androidx.lifecycle.*
import com.cleanairspaces.android.models.entities.SearchSuggestions
import com.cleanairspaces.android.models.repository.ScannedDevicesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

@HiltViewModel
class SearchLocationActViewModel @Inject constructor(
    private val scannedDevicesRepo: ScannedDevicesRepo
) : ViewModel() {

    private val TAG  = SearchLocationActViewModel::class.java.simpleName

    private val searchQuery = MutableLiveData<String>()
    fun getSuggestions() : LiveData<List<SearchSuggestions>> = searchQuery.switchMap {
        if (it.isBlank()) emptyFlow<List<SearchSuggestions>>().asLiveData()
        else scannedDevicesRepo.getSearchSuggestions(it).asLiveData()
    }
    fun search(query: String) {
        searchQuery.value = query
    }


}