package com.cleanairspaces.android.models.dao

import androidx.room.*
import com.cleanairspaces.android.models.entities.LocationHistoryMonth
import com.cleanairspaces.android.models.entities.LocationHistoryThreeDays
import com.cleanairspaces.android.models.entities.LocationHistoryUpdatesTracker
import com.cleanairspaces.android.models.entities.LocationHistoryWeek
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationHistoryThreeDaysDao {

    @Query("DELETE FROM location_history_last_three")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationHistoryThreeDays: List<LocationHistoryThreeDays>)

    @Query("SELECT * FROM location_history_last_three WHERE forScannedDeviceId =:forScannedDeviceId ORDER BY date_reading DESC")
    fun getLastDaysHistory(forScannedDeviceId : String) : Flow<List<LocationHistoryThreeDays>>
}

@Dao
interface LocationHistoryWeekDao{

    @Query("DELETE FROM location_history_last_week")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationHistoryWeek: List<LocationHistoryWeek>)

    @Query("SELECT * FROM location_history_last_week WHERE forScannedDeviceId =:forScannedDeviceId ORDER BY date_reading DESC")
    fun getLastWeekHistory(forScannedDeviceId : String)  : Flow<List<LocationHistoryWeek>>
}

@Dao
interface LocationHistoryMonthDao{

    @Query("DELETE FROM location_history_last_month")
    suspend fun deleteAll()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationHistoryMonth: List<LocationHistoryMonth>)

    @Query("SELECT * FROM location_history_last_month WHERE forScannedDeviceId =:forScannedDeviceId ORDER BY date_reading DESC")
    fun getLastMonthHistory(forScannedDeviceId : String) : Flow<List<LocationHistoryMonth>>
}

@Dao
interface LocationHistoryUpdatesTrackerDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewOrReplace(locationHistoryUpdatesTracker  : LocationHistoryUpdatesTracker)

    @Query("SELECT lastUpdated FROM location_history_updates_tracker WHERE forScannedDeviceId =:deviceId")
    suspend fun getLastUpdatedTimeForDevice(deviceId : String) : Long?
}