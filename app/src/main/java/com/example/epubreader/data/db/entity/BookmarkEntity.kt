package com.example.epubreader.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: String,
    val chapterIndex: Int,
    val textPosition: Int,
    val dateCreated: Long = System.currentTimeMillis(),
    val note: String? = null
)
