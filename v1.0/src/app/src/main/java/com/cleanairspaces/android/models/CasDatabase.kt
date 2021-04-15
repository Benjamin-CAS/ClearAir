package com.cleanairspaces.android.models

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cleanairspaces.android.models.dao.*
import com.cleanairspaces.android.models.entities.*

@Database(
        entities = [
            OutDoorLocations::class,
            LocationDataFromQr::class,
            LocationDetails::class,
            LocationHistoryThreeDays::class,
            LocationHistoryWeek::class,
            LocationHistoryMonth::class,
            LocationHistoryUpdatesTracker::class],
        version = 1,
        exportSchema = false
)
@TypeConverters(
        CustomTypeConverters::class
)
abstract class CasDatabase : RoomDatabase() {
    abstract fun outDoorLocationsDao(): OutDoorLocationsDao
    abstract fun customerDeviceDataDao(): LocDataFromQrDao
    abstract fun myLocationDetailsDao(): LocationDetailsDao
    abstract fun locationHistoryThreeDaysDao(): LocationHistoryThreeDaysDao
    abstract fun locationHistoryWeekDao(): LocationHistoryWeekDao
    abstract fun locationHistoryMonthDao(): LocationHistoryMonthDao
    abstract fun locationHistoryUpdatesTrackerDao() : LocationHistoryUpdatesTrackerDao
}