package com.example.epubreader.domain.model

import androidx.annotation.Keep
import com.example.epubreader.data.model.ReadingPreferences
import java.util.Date

@Keep
data class EpubBooks(
    val id: String,
    val title: String,
    val chapters: List<Chapter>,
    val currentPosition: ReadingPosition,
    val readingPreferences: ReadingPreferences,
    val dateAdded: Date,
    val lastReadDate: Date
)
