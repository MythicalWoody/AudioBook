package com.example.epubreader.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.epubreader.data.model.ReadingPreferences

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["title"]),
        Index(value = ["lastReadDate"]),
        Index(value = ["dateAdded"])
    ]
)
data class BookEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val filePath: String,
    val lastReadChapterIndex: Int = 0,
    val lastReadPosition: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastReadDate: Long = System.currentTimeMillis()
)
