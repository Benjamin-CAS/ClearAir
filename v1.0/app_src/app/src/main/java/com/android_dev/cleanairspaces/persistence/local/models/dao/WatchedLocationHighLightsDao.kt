package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.*
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import kotlinx.coroutines.flow.Flow


@Dao
interface WatchedLocationHighLightsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(watchedLocationHighLights: List<WatchedLocationHighLights>)

    @Query("SELECT * FROM watched_location ORDER BY name ASC")
    fun getWatchedLocationHighLights(): Flow<List<WatchedLocationHighLights>>

    @Query("SELECT * FROM watched_location ORDER BY last_updated DESC")
    suspend fun getWatchedLocationHighLightsOnce(): List<WatchedLocationHighLights>

    @Query("DELETE FROM watched_location")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteWatchedLocationHighLights(watchedLocationHighLights: WatchedLocationHighLights)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(watchedLocationHighLights: WatchedLocationHighLights)

    @Query("SELECT * FROM watched_location WHERE actualDataTag =:actualDataTag LIMIT 1")
    suspend fun checkIfIsWatchedLocation(actualDataTag: String): List<WatchedLocationHighLights>


}