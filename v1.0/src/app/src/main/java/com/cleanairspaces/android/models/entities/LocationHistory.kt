package com.cleanairspaces.android.models.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
        tableName = "location_history_last_three",
)
data class LocationHistoryThreeDays(
        @PrimaryKey(autoGenerate = true)
        var autoId: Int,
        var forScannedDeviceId: String = "",
        @Embedded
        var data: CommonHistoryData
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
        @Embedded
        var data: CommonHistoryData
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
        @Embedded
        var data: CommonHistoryData
) : Parcelable {

    companion object {
        const val responseKey = "lastMonth"
    }
}

@Parcelize
data class CommonHistoryData(
        val date_reading: String,
        val avg_reading: Long,
        val avg_tvoc: String,
        val avg_co2: Long,
        val avg_temperature: Long,
        val avg_humidity: Long,
        val reading_comp: Long,
) : Parcelable


@Parcelize
@Entity(tableName = "location_history_updates_tracker")
data class LocationHistoryUpdatesTracker(
        @PrimaryKey(autoGenerate = false)
        var forScannedDeviceId: String = "",
        val lastUpdated: Long = System.currentTimeMillis()
) : Parcelable
