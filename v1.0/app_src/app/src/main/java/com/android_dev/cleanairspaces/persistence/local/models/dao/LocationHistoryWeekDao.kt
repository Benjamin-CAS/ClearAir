package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android_dev.cleanairspaces.persistence.local.models.entities.LocationHistoryWeek
import kotlinx.coroutines.flow.Flow
import java.util.ArrayList

@Dao
interface LocationHistoryWeekDao {
    @Query("SELECT * FROM location_history_last_week WHERE actualDataTag =:dataTag")
    fun getLastWeekHistoryFlow(dataTag: String) : Flow<List<LocationHistoryWeek>>

    @Query("DELETE FROM location_history_last_week WHERE actualDataTag =:dataTag")
    suspend fun deleteAllHistoriesForData(dataTag: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(weekHistory: List<LocationHistoryWeek>)
}