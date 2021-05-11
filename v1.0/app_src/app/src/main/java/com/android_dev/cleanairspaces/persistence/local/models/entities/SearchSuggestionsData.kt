package com.android_dev.cleanairspaces.persistence.local.models.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
        tableName = "search_suggestions",
)
data class SearchSuggestionsData(
        @PrimaryKey(autoGenerate = false)
        val actualDataTag: String,
        val nameToDisplay: String,
        var isForIndoorLoc: Boolean,
        var isForMonitor: Boolean,
        var isForOutDoorLoc: Boolean,
        val location_id: String,
        val monitor_id: String,
        val company_id: String,
        var is_secure: Boolean,
        var lat: Double? = null,
        var lon: Double? = null
) : Parcelable