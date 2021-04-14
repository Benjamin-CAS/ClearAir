package com.cleanairspaces.android.models.api

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cleanairspaces.android.models.dao.LocDataFromQrDao
import com.cleanairspaces.android.models.dao.LocationDetailsDao
import com.cleanairspaces.android.models.dao.OutDoorLocationsDao
import com.cleanairspaces.android.models.entities.CustomTypeConverters
import com.cleanairspaces.android.models.entities.LocationDataFromQr
import com.cleanairspaces.android.models.entities.LocationDetails
import com.cleanairspaces.android.models.entities.OutDoorLocations

@Database(
    entities = [OutDoorLocations::class, LocationDataFromQr::class, LocationDetails::class],
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
}