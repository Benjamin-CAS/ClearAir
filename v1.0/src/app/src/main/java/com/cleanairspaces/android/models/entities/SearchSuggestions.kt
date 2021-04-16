package com.cleanairspaces.android.models.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(tableName = "search_suggestions",
        indices = [androidx.room.Index(
            value = ["location_name"],
            unique = true
        )]
)
@Parcelize
data class SearchSuggestions(
    @PrimaryKey(autoGenerate = true)
    var autoId: Int,

    val company_id: String = "",
    val location_id: String = "",
    val location_name: String,
    val monitor_id: String = ""

):Parcelable