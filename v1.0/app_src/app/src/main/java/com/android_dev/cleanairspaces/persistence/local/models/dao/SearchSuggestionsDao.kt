package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchSuggestionsDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestions(searchSuggestionsData: List<SearchSuggestionsData>)

    @Query("SELECT * FROM search_suggestions WHERE nameToDisplay LIKE :query")
    fun getSearchSuggestions(query: String): Flow<List<SearchSuggestionsData>>

    @Query("DELETE FROM search_suggestions WHERE isForOutDoorLoc =:isTrue")
    suspend fun deleteAllOutDoorSearchSuggestions(isTrue: Boolean = true)

    @Query("DELETE FROM search_suggestions WHERE isForIndoorLoc =:isTrue")
    suspend fun deleteAllInDoorSearchSuggestions(isTrue: Boolean = true)

    @Query("DELETE FROM search_suggestions WHERE isForMonitor =:isTrue")
    suspend fun deleteAllMonitorSearchSuggestions(isTrue: Boolean = true)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(searchSuggestionsData: SearchSuggestionsData)
}