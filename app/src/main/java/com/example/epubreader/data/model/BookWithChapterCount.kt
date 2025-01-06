package com.example.epubreader.data.model

import androidx.room.Embedded
import com.example.epubreader.data.db.entity.BookEntity

data class BookWithChapterCount(
    @Embedded val book: BookEntity,
    val chapterCount: Int
)
