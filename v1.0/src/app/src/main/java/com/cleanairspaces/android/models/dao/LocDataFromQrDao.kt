package com.cleanairspaces.android.models.dao

import androidx.room.*
import com.cleanairspaces.android.models.entities.LocationDataFromQr
import kotlinx.coroutines.flow.Flow
import java.util.ArrayList

@Dao
interface LocDataFromQrDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(customerDeviceData: LocationDataFromQr)

    @Query("SELECT * FROM location_data_from_qr WHERE autoId =:deviceId")
    suspend fun getDeviceById(deviceId: Int) : LocationDataFromQr?

    @Update
    suspend fun updateDevice(deviceData: LocationDataFromQr)

    @Query("SELECT COUNT(autoId) FROM location_data_from_qr WHERE company_id=:compId AND location_id=:locId AND monitor_id =:monitorId AND is_mine=:isMine")
    suspend fun checkIfIsMyLocation(compId: String, locId: String, monitorId: String, isMine :Boolean = true): Int

    @Query("SELECT * FROM location_data_from_qr WHERE company_id=:compId AND location_id=:locId  AND is_mine =:isTrue LIMIT 1")
    suspend fun getMyDeviceBy(compId: String, locId: String, isTrue : Boolean = true): List<LocationDataFromQr>


    @Query("SELECT * FROM location_data_from_qr WHERE company_id=:companyId AND location_id=:locationId AND is_indoor_location =:isIndoorLoc LIMIT 1")
    fun getOutDoorLocDeviceFlow(companyId: String, locationId: String, isIndoorLoc : Boolean = false): Flow<LocationDataFromQr>


    @Query("SELECT * FROM location_data_from_qr WHERE monitor_id =:monitorId AND is_indoor_location =:isIndoorLoc LIMIT 1")
    fun getAMonitorDeviceFlow(monitorId: String, isIndoorLoc : Boolean = false): Flow<LocationDataFromQr>


    @Query("SELECT * FROM location_data_from_qr WHERE company_id =:compId AND is_indoor_location =:isIndoorLoc LIMIT 1")
    fun getAnIndoorLocation(compId: Int, isIndoorLoc : Boolean = true): Flow<LocationDataFromQr>

    @Query("SELECT * FROM location_data_from_qr WHERE company_id =:compId AND location_id =:locId AND monitor_id=:monitorId LIMIT 1")
    suspend fun getDeviceBoundToLocation(compId: String, locId: String, monitorId: String): List<LocationDataFromQr>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locationDatas: List<LocationDataFromQr>)


}