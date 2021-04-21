package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.*
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import kotlinx.coroutines.flow.Flow


@Dao
interface WatchedLocationHighLightsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(watchedLocationHighLights: List<WatchedLocationHighLights>)

    @Query("SELECT * FROM watched_location ORDER BY last_updated DESC")
    fun getWatchedLocationHighLights(): Flow<List<WatchedLocationHighLights>>

    @Query("DELETE FROM watched_location")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteWatchedLocationHighLights(watchedLocationHighLights: WatchedLocationHighLights)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(watchedLocationHighLights: WatchedLocationHighLights)

    @Query("SELECT COUNT(actualDataTag) FROM watched_location WHERE actualDataTag =:actualDataTag ")
    suspend fun checkIfIsWatchedLocation(actualDataTag: String): Int

}