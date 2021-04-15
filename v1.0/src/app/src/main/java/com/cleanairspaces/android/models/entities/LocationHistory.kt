package com.cleanairspaces.android.models.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import javax.annotation.Nullable

@Parcelize
@Entity(
        tableName = "location_history_last_three",
)
data class LocationHistoryThreeDays(
        @PrimaryKey(autoGenerate = true)
        var autoId: Int,
        var forScannedDeviceId: String = "",
        var date_reading: String?,
        var avg_reading: Long?,
        var avg_tvoc: String?,
        var avg_co2: Long?,
        var avg_temperature: Long?,
        var avg_humidity: Long?,
        var reading_comp: Long?,
) : Parcelable {

    companion object {
        const val responseKey = "latest72h"
    }
}

@Parcelize
@Entity(
        tableName = "location_history_last_week",
)
data class LocationHistoryWeek(
        @PrimaryKey(autoGenerate = true)
        var autoId: Int,
        var forScannedDeviceId: String = "",
        var date_reading: String?,
        var avg_reading: Long?,
        var avg_tvoc: String?,
        var avg_co2: Long?,
        var avg_temperature: Long?,
        var avg_humidity: Long?,
        var reading_comp: Long?,
) : Parcelable {

    companion object {
        const val responseKey = "lastWeek"
    }
}

@Parcelize
@Entity(
        tableName = "location_history_last_month",
)
data class LocationHistoryMonth(
        @PrimaryKey(autoGenerate = true)
        var autoId: Int,
        var forScannedDeviceId: String = "",
        var date_reading: String?,
        var avg_reading: Long?,
        var avg_tvoc: String?,
        var avg_co2: Long?,
        var avg_temperature: Long?,
        var avg_humidity: Long?,
        var reading_comp: Long?,
) : Parcelable {

    companion object {
        const val responseKey = "lastMonth"
    }
}


@Parcelize
@Entity(tableName = "location_history_updates_tracker")
data class LocationHistoryUpdatesTracker(
        @PrimaryKey(autoGenerate = false)
        var forScannedDeviceId: String = "",
        var lastUpdated: Long = System.currentTimeMillis()
) : Parcelable
