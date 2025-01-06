package com.example.epubreader.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId", "indexInBook"]),
        Index(value = ["bookId", "title"])
    ]
)
data class ChapterEntity(
    @PrimaryKey
    val id: String,
    val bookId: String,
    val title: String,
    val content: String,
    val indexInBook: Int
)
