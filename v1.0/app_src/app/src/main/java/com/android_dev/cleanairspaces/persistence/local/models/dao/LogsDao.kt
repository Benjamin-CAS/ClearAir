package com.android_dev.cleanairspaces.persistence.local.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android_dev.cleanairspaces.persistence.local.models.entities.Logs

@Dao
interface LogsDao {

    @Insert
    suspend fun insertLog(log: Logs)

    @Query("SELECT * FROM logs ORDER BY recordedAt DESC")
    suspend fun getLogs(): List<Logs>

    @Query("DELETE FROM logs")
    suspend fun clearLogData()

}