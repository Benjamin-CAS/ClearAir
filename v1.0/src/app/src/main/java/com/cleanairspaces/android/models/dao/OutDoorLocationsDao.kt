package com.cleanairspaces.android.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cleanairspaces.android.models.entities.OutDoorLocations
import kotlinx.coroutines.flow.Flow

@Dao
interface OutDoorLocationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutDoorLocation(locations: OutDoorLocations)

    @Query("SELECT * FROM out_door_locations")
    fun getOutDoorLocationsFlow(): Flow<List<OutDoorLocations>>
}
