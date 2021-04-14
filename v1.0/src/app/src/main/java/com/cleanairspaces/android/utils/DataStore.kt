package com.cleanairspaces.android.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cleanairspaces.android.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// At the top level of your kotlin file:
private val Context.dataStore by preferencesDataStore("settings")

class DataStoreManager(appContext: Context) {


    private val mDataStore = appContext.dataStore
    private val defaultAqi = appContext.getString(R.string.default_pm_index_value)
    private val defaultMap = appContext.getString(R.string.a_map_preferred)

    fun getAqiIndex(): Flow<String> {
        return mDataStore.data
            .map { preferences ->
                preferences[aqiIndexKey] ?: defaultAqi
            }
    }

    suspend fun saveAqiIndex(newAqiIndex: String) {
        mDataStore.edit { settings ->
            settings[aqiIndexKey] = newAqiIndex
        }
    }


    fun getSelectedMap(): Flow<String> {
        return mDataStore.data
            .map { preferences ->
                preferences[mapToUseKey] ?: defaultMap
            }
    }

    private val aqiIndexKey = stringPreferencesKey("api_index")
    private val mapToUseKey = stringPreferencesKey("map_to_use")

}