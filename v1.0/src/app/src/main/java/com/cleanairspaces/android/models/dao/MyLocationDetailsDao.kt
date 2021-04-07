package com.cleanairspaces.android.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.cleanairspaces.android.models.entities.MyLocationDetails

@Dao
interface MyLocationDetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyLocationDetails(myLocationDetails: MyLocationDetails)

}