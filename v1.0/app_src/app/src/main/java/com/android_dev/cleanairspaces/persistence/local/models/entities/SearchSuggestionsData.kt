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
    var isForIndoorLoc: Boolean = false,
    var isForMonitor: Boolean = false,
    var isForOutDoorLoc: Boolean = false
) : Parcelable