package com.dluvian.nozzle.data.room

import androidx.room.TypeConverter

class Converters {
    private val SEPARATOR = "{$}"

    @TypeConverter
    fun fromListToString(value: List<String>?): String? {
        return value?.joinToString(separator = SEPARATOR)
    }

    @TypeConverter
    fun fromStringToList(value: String?): List<String> {
        return value?.split(SEPARATOR) ?: emptyList()
    }
}
