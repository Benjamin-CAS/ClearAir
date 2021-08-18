package com.android_dev.cleanairspaces.persistence.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android_dev.cleanairspaces.utils.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// At the top level_intensity of your kotlin file:
private val Context.dataStore by preferencesDataStore(SETTINGS_FILE_NAME)

class DataStoreManager(appContext: Context) {

    private val mDataStore = appContext.dataStore

    fun getAqiIndex(): Flow<String?> {
        return mDataStore.data
            .map { preferences ->
                preferences[aqiIndexKey]
            }
    }


    suspend fun saveAqiIndex(newAqiIndex: String) {
        mDataStore.edit { settings ->
            settings[aqiIndexKey] = newAqiIndex
        }
    }

    suspend fun saveMap(selectedMap: String) {
        mDataStore.edit { settings ->
            settings[mapToUseKey] = selectedMap
        }
    }


    fun getSelectedMap(): Flow<String?> {
        return mDataStore.data
            .map { preferences ->
                preferences[mapToUseKey]
            }
    }

    suspend fun saveMapLang(selectedMapLang: String) {
        mDataStore.edit { settings ->
            settings[mapLang] = selectedMapLang
        }
    }

    fun getMapLang(): Flow<String?> {
        return mDataStore.data
            .map { preferences ->
                preferences[mapLang]
            }
    }

    suspend fun setAlreadyAskedLocPermission() {
        mDataStore.edit { settings ->
            settings[hasRequestedLocationKey] = true
        }
    }

    fun hasAlreadyAskedLocPermission(): Flow<Boolean> {
        return mDataStore.data
            .map { preferences ->
                preferences[hasRequestedLocationKey] ?: false
            }
    }

    private val aqiIndexKey = stringPreferencesKey(AQI_INDEX_TO_USE_KEY)
    private val mapToUseKey = stringPreferencesKey(MAP_TO_USE_KEY)
    private val mapLang = stringPreferencesKey(MAP_LANG_TO_USE_KEY)
    private val hasRequestedLocationKey = booleanPreferencesKey(HAS_REQUESTED_LOC_PERMISSION)

}