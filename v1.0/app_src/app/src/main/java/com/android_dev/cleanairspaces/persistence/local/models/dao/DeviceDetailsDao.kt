package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDetailsDao {

    @Query("SELECT * FROM devices_data WHERE for_watched_location_tag =:locationsTag ORDER BY dev_name ASC")
    fun observeDevicesForLocation(locationsTag: String): Flow<List<DevicesDetails>>

    @Query("SELECT * FROM devices_data WHERE watch_device =:watchLocation ORDER BY dev_name ASC")
    fun observeWatchedDevices(watchLocation: Boolean = true): Flow<List<DevicesDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(devices: List<DevicesDetails>)

    @Query("UPDATE devices_data SET watch_device =:watchDevice WHERE actualDataTag =:devicesTag ")
    suspend fun toggleIsWatched(watchDevice: Boolean, devicesTag: String)

    @Query("SELECT * FROM devices_data WHERE watch_device =:watchLocation ORDER BY updated_on ASC")
    suspend fun getAllWatchedDevicesOnce(watchLocation: Boolean = true): List<DevicesDetails>

    @Query("SELECT * FROM devices_data")
    suspend fun getAllDevicesNonObservable(): List<DevicesDetails>

    @Query("SELECT * FROM devices_data WHERE id =:deviceId AND watch_device =:isWatched")
    suspend fun checkIfIsWatched(deviceId: String, isWatched: Boolean = true): List<DevicesDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(devicesDetails: DevicesDetails)

}