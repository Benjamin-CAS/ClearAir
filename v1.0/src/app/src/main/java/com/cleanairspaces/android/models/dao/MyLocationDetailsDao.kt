package com.cleanairspaces.android.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cleanairspaces.android.models.entities.MyLocationDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface MyLocationDetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyLocationDetails(myLocationDetails: MyLocationDetails)

    @Query("SELECT * FROM my_location_details ORDER BY lastUpdated DESC")
    fun getMyLocationsFlow(): Flow<List<MyLocationDetails>>

}