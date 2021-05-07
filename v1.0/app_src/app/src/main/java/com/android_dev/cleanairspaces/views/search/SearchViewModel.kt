package com.android_dev.cleanairspaces.views.search

import androidx.lifecycle.*
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

@HiltViewModel
class SearchLocationViewModel @Inject constructor(
        private val appDataRepo: AppDataRepo,
        private val myLogger: MyLogger

) : ViewModel() {


    private val TAG = SearchLocationViewModel::class.java.simpleName

    private val searchQuery = MutableLiveData<String>()
    fun getSuggestions(): LiveData<List<SearchSuggestionsData>> = searchQuery.switchMap {
        if (it.isNullOrBlank()) emptyFlow<List<SearchSuggestionsData>>().asLiveData()
        else appDataRepo.getSearchSuggestions(it).asLiveData()
    }

    fun search(query: String) {
        searchQuery.value = query
        if (query.length > 2) {
            myLogger.logThis(
                    tag = LogTags.USER_ACTION_SEARCH,
                    from = TAG,
                    msg = "searching... for $query"
            )
        }
    }

}