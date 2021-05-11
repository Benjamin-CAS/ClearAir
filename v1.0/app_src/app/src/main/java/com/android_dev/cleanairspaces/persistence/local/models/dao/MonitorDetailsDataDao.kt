package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitorDetailsDataDao {

    @Query("SELECT * FROM monitors_data WHERE for_watched_location_tag =:locationsTag ORDER BY updated_on DESC")
    fun observeMonitorsForLocation(locationsTag : String): Flow<List<MonitorDetails>>

    @Query("SELECT * FROM monitors_data WHERE watch_location =:watchLocation ORDER BY indoor_name_en")
    fun observeWatchedMonitors(watchLocation : Boolean = true): Flow<List<MonitorDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(monitors: List<MonitorDetails>)

    @Query("UPDATE monitors_data SET watch_location =:watchLocation WHERE actualDataTag =:monitorsTag ")
    suspend fun toggleIsWatched(watchLocation : Boolean, monitorsTag : String)

    @Query("SELECT * FROM monitors_data WHERE watch_location =:watchLocation ORDER BY updated_on ASC")
    suspend fun getAllWatchedMonitorsOnce(watchLocation: Boolean = true): List<MonitorDetails>

    @Query("UPDATE monitors_data SET indoor_co2 =:co2 AND indoor_humidity =:humid AND indoor_pm_25=:inPm AND indoor_temperature=:tmp AND indoor_display_param =:inDisplayParam AND indoor_tvoc =:tvoc AND outdoor_pm =:outPm AND outdoor_display_param =:outDisplayParam AND updated_on =:updatedOn WHERE monitor_id =:id ")
    suspend fun updateDetailsForMonitor(id: String, co2: Double?, inPm: Double?, tmp: Double?, humid: Double?, tvoc: Double?, inDisplayParam: String?, outPm: Double?, outDisplayParam: String?, updatedOn : Long)

    @Query("SELECT * FROM monitors_data WHERE monitor_id =:monitorId AND watch_location =:isWatched")
    suspend fun checkIfIsWatched(monitorId: String, isWatched : Boolean = true): List<MonitorDetails>

}