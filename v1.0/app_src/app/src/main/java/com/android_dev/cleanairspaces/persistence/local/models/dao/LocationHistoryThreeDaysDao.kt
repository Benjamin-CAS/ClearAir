package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android_dev.cleanairspaces.persistence.local.models.entities.LocationHistoryThreeDays
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationHistoryThreeDaysDao {

    @Query("SELECT * FROM location_history_last_three WHERE actualDataTag =:dataTag")
    fun getLastDaysHistoryFlow(dataTag: String): Flow<List<LocationHistoryThreeDays>>

    @Query("DELETE FROM location_history_last_three WHERE actualDataTag =:dataTag")
    suspend fun deleteAllHistoriesForData(dataTag: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(daysHistory: List<LocationHistoryThreeDays>)
}