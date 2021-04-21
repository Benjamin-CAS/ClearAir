package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android_dev.cleanairspaces.persistence.local.models.entities.MapData
import kotlinx.coroutines.flow.Flow


@Dao
interface MapDataDao {

    @Query("SELECT * FROM map_displayed_data ORDER BY last_updated DESC")
    suspend fun getAllMapData(): List<MapData>

    @Query("SELECT * FROM map_displayed_data ORDER BY last_updated DESC")
    fun getMapDataFlow(): Flow<List<MapData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(newMapData: List<MapData>)

}