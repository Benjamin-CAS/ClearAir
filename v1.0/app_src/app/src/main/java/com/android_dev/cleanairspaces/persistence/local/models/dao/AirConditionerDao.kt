package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android_dev.cleanairspaces.persistence.local.models.entities.AirConditionerEntity
import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails
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
}