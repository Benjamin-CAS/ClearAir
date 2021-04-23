package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android_dev.cleanairspaces.persistence.local.models.entities.LocationHistoryMonth
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationHistoryMonthDao {

    @Query("SELECT * FROM location_history_last_month WHERE actualDataTag =:dataTag")
    fun getLastMonthsHistoryFlow(dataTag: String): Flow<List<LocationHistoryMonth>>

    @Query("DELETE FROM location_history_last_month WHERE actualDataTag =:dataTag")
    suspend fun deleteAllHistoriesForData(dataTag: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(monthHistory: List<LocationHistoryMonth>)
}