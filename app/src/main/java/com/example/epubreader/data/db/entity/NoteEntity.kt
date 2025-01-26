package com.example.epubreader.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val bookId: String,
    val chapterIndex: Int,
    val position: Int,
    val content: String,
    val dateCreated: Date,
    val dateModified: Date
)
