package com.cleanairspaces.android.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cleanairspaces.android.models.entities.SearchSuggestions
import kotlinx.coroutines.flow.Flow


@Dao
interface SearchSuggestionsDao {

    @Query("SELECT * FROM search_suggestions WHERE location_name LIKE :query")
    fun getSearchSuggestions(query: String) : Flow<List<SearchSuggestions>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(searchSuggestions: List<SearchSuggestions>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchSuggestions: SearchSuggestions)
}