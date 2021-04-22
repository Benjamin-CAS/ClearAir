package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface LocationHistoryUpdatesTrackerDao {

    @Query("SELECT lastUpdated FROM location_history_updates_tracker WHERE actualDataTag =:dataTag")
    suspend fun checkLastUpdate(dataTag: String): Long?
}