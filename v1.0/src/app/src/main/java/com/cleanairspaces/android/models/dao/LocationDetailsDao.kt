package com.cleanairspaces.android.models.dao

import androidx.room.*
import com.cleanairspaces.android.models.entities.LocationDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationDetails: LocationDetails)

    @Query("SELECT * FROM my_location_details WHERE bound_to_scanned_device_id =:deviceId")
    suspend fun getDetailsForDeviceWithQr(deviceId: String) : List<LocationDetails>

    @Update
    suspend fun update(locationsDetails: LocationDetails)

    @Query("SELECT * FROM my_location_details WHERE is_mine =:isTrue ")
    fun getMyLocationsFlow(isTrue : Boolean = true): Flow<List<LocationDetails>>

    @Query("SELECT * FROM my_location_details WHERE is_mine =:isTrue")
    suspend fun getAllMyLocationsOnce(isTrue: Boolean = true): List<LocationDetails>

    @Delete
    suspend fun deleteAll(locationsDetails: List<LocationDetails>)


}