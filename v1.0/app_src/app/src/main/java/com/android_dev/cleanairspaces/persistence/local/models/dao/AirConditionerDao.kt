package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.*
import com.android_dev.cleanairspaces.persistence.local.models.entities.AirConditionerEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Benjamin
 * @description:
 * @date :2021.10.14 9:41
 */
@Dao
interface AirConditionerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAirConditionerDao(ariConditioner:List<AirConditionerEntity>)
    @Query("SELECT * FROM AIR_CONDITIONER")
    fun getAirConditionerAll():Flow<List<AirConditionerEntity>>
    @Query("SELECT * FROM AIR_CONDITIONER WHERE watchAirConditioner =:watched ORDER BY devName ASC")
    fun observeAirConditionerIWatch(watched: Boolean = true): Flow<List<AirConditionerEntity>>
    @Query("UPDATE AIR_CONDITIONER SET watchAirConditioner =:watchAirConditioner WHERE id =:devicesTag")
    suspend fun toggleIsWatched(watchAirConditioner: Boolean, devicesTag: Int):Int
    @Query("DELETE FROM AIR_CONDITIONER")
    suspend fun deleteAllAirConditioner()
    @Query("SELECT COUNT(id) FROM AIR_CONDITIONER WHERE id =:deviceId AND watchAirConditioner =:isWatched")
    suspend fun checkIfIsWatched(deviceId: Int, isWatched: Boolean = true): Int
}