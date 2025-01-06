package com.example.epubreader.data.db.convertor

import androidx.room.TypeConverter
import com.example.epubreader.data.model.ReadingPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ReadingPreferencesConverter {
    @TypeConverter
    fun fromJson(value: String): ReadingPreferences {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun toJson(preferences: ReadingPreferences): String {
        return Json.encodeToString(preferences)
    }
}