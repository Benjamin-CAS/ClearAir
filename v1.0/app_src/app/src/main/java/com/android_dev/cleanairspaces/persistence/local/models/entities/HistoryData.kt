package com.android_dev.cleanairspaces.persistence.local.models.entities

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
    @Embedded val data: HistoryData
) : Parcelable

@Parcelize
@Entity(
    tableName = "location_history_last_week",
)
data class LocationHistoryWeek(
    @PrimaryKey(autoGenerate = true)
    var autoId: Int,
    @Embedded val data: HistoryData
) : Parcelable


@Parcelize
@Entity(
    tableName = "location_history_last_month",
)
data class LocationHistoryMonth(
    @PrimaryKey(autoGenerate = true)
    var autoId: Int,
    @Embedded val data: HistoryData
) : Parcelable

@Parcelize
@Entity(tableName = "location_history_updates_tracker")
data class LocationHistoryUpdatesTracker(
    @PrimaryKey(autoGenerate = false)
    val actualDataTag: String,
    var lastUpdated: Long = System.currentTimeMillis()
) : Parcelable


@Parcelize
data class HistoryData(
    var actualDataTag: String,
    var dates: String,
    var indoor_pm: Float,
    var tvoc: Float,
    var co2: Float,
    var temperature: Float,
    var humidity: Float,
    var outdoor_pm: Float
) : Parcelable