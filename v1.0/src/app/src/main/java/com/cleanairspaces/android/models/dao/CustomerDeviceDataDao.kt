package com.cleanairspaces.android.models.dao

import androidx.room.*
import com.cleanairspaces.android.models.entities.CustomerDeviceData
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDeviceDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceData(deviceData: CustomerDeviceData)

    @Query("SELECT * FROM customer_device_data WHERE isMyDeviceData =:isTrue ")
    fun getMyDevicesFlow(isTrue : Boolean = true): Flow<List<CustomerDeviceData>>


    @Query("SELECT * FROM customer_device_data WHERE company_id =:companyId AND location_id =:locationId ")
    fun getADeviceFlow(companyId : String, locationId  : String): Flow<CustomerDeviceData>

    @Query("DELETE FROM customer_device_data")
    suspend fun deleteAllDevices()

    @Update
    suspend fun updateDevice(deviceData: CustomerDeviceData)

    
}