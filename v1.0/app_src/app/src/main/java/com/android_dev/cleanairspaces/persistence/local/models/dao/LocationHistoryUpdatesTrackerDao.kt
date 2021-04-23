package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android_dev.cleanairspaces.persistence.local.models.entities.LocationHistoryUpdatesTracker

@Dao
interface LocationHistoryUpdatesTrackerDao {

    @Query("SELECT lastUpdated FROM location_history_updates_tracker WHERE actualDataTag =:dataTag")
    suspend fun checkLastUpdate(dataTag: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLastUpdate(locationHistoryUpdatesTracker: LocationHistoryUpdatesTracker)
}