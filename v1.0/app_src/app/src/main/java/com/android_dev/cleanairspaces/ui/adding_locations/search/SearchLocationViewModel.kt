package com.android_dev.cleanairspaces.ui.adding_locations.search

import androidx.lifecycle.*
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

@HiltViewModel
class SearchLocationViewModel @Inject constructor(
        private val appDataRepo: AppDataRepo

) : ViewModel() {


    private val TAG = SearchLocationAct::class.java.simpleName

    private val searchQuery = MutableLiveData<String>()
    fun getSuggestions(): LiveData<List<SearchSuggestionsData>> = searchQuery.switchMap {
        if (it.isNullOrBlank()) emptyFlow<List<SearchSuggestionsData>>().asLiveData()
        else appDataRepo.getSearchSuggestions(it).asLiveData()
    }

    fun search(query: String) {
        searchQuery.value = query
    }

}