package com.cleanairspaces.android.models.entities

import androidx.room.TypeConverter

class CustomTypeConverters {
    @TypeConverter
    fun toLocationArea(enumName: String) = enumValueOf<LocationAreas>(enumName)

    @TypeConverter
    fun fromLocationArea(locationAreas: LocationAreas) = locationAreas.name

}