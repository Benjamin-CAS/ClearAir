package com.android_dev.cleanairspaces.persistence.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android_dev.cleanairspaces.persistence.local.models.dao.*
import com.android_dev.cleanairspaces.persistence.local.models.entities.*

@Database(
    entities = [
        MapData::class,
        SearchSuggestionsData::class,
        WatchedLocationHighLights::class,
        LocationHistoryThreeDays::class,
        LocationHistoryWeek::class,
        LocationHistoryMonth::class,
        LocationHistoryUpdatesTracker::class,
        Logs::class],
    version = 1,
    exportSchema = false
)
abstract class CasDatabase : RoomDatabase() {
    abstract fun mapDataDao(): MapDataDao
    abstract fun searchSuggestionsDataDao(): SearchSuggestionsDataDao
    abstract fun watchedLocationHighLightsDao(): WatchedLocationHighLightsDao
    abstract fun locationHistoryThreeDaysDao(): LocationHistoryThreeDaysDao
    abstract fun locationHistoryWeekDao(): LocationHistoryWeekDao
    abstract fun locationHistoryMonthDao(): LocationHistoryMonthDao
    abstract fun locationHistoryUpdatesTrackerDao(): LocationHistoryUpdatesTrackerDao
    abstract fun logsDao(): LogsDao
}