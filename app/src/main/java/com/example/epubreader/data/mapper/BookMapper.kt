package com.example.epubreader.data.mapper

import com.example.epubreader.data.db.entity.BookEntity
import com.example.epubreader.data.db.entity.ChapterEntity
import com.example.epubreader.data.model.ReadingPreferences
import com.example.epubreader.data.model.TextAlignment
import com.example.epubreader.data.model.Theme
import com.example.epubreader.domain.model.Chapter
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.model.ReadingPosition
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class BookMapper @Inject constructor() {
    fun mapToEntity(bookEntity: BookEntity, chapters: List<ChapterEntity>): EpubBooks {
        return EpubBooks(
            id = bookEntity.id,
            title = bookEntity.title,
            chapters = chapters.map { mapChapterToModel(it) },
            currentPosition = ReadingPosition(
                chapterIndex = bookEntity.lastReadChapterIndex,
                textPosition = bookEntity.lastReadPosition
            ),
            readingPreferences = bookEntity.readingPreferences,
            dateAdded = Date(bookEntity.dateAdded),
            lastReadDate = Date(bookEntity.lastReadDate),
            coverImage = bookEntity.coverImage
        )
    }

    fun mapChapterToEntity(chapter: Chapter, bookId: String, index: Int): ChapterEntity {
        return ChapterEntity(
            id = UUID.randomUUID().toString(),
            bookId = bookId,
            title = chapter.title,
            content = chapter.content,
            indexInBook = index
        )
    }

    private fun mapChapterToModel(chapterEntity: ChapterEntity): Chapter {
        return Chapter(
            id = chapterEntity.id,
            title = chapterEntity.title,
            content = chapterEntity.content
        )
    }
}
