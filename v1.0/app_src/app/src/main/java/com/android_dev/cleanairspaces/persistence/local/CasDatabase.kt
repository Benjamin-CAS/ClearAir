package com.android_dev.cleanairspaces.persistence.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android_dev.cleanairspaces.persistence.local.models.dao.MapDataDao
import com.android_dev.cleanairspaces.persistence.local.models.dao.SearchSuggestionsDataDao
import com.android_dev.cleanairspaces.persistence.local.models.dao.WatchedLocationHighLightsDao
import com.android_dev.cleanairspaces.persistence.local.models.entities.MapData
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights

@Database(
    entities = [MapData::class, SearchSuggestionsData::class, WatchedLocationHighLights::class],
    version = 1,
    exportSchema = false
)
abstract class CasDatabase : RoomDatabase() {
    abstract fun mapDataDao(): MapDataDao
    abstract fun searchSuggestionsDataDao(): SearchSuggestionsDataDao
    abstract fun watchedLocationHighLightsDao(): WatchedLocationHighLightsDao
}