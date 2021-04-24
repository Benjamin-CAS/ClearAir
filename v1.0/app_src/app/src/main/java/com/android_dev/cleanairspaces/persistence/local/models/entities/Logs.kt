package com.android_dev.cleanairspaces.persistence.local.models.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "logs")
@Parcelize
data class Logs(
    @PrimaryKey(autoGenerate = true)
    val id : Int,

    val key : String,
    val message : String,
    val tag : String,
    val  last_updated: Long = System.currentTimeMillis()

):Parcelable