package com.example.epubreader.data.mapper

import com.example.epubreader.data.db.entity.HighlightEntity
import com.example.epubreader.domain.model.Highlight

fun HighlightEntity.toDomain(): Highlight {
    return Highlight(
        id = id,
        bookId = bookId,
        chapterIndex = chapterIndex,
        startPosition = startPosition,
        endPosition = endPosition,
        highlightedText = highlightedText,
        color = color,
        dateCreated = dateCreated
    )
}

fun Highlight.toEntity(): HighlightEntity {
    return HighlightEntity(
        id = id,
        bookId = bookId,
        chapterIndex = chapterIndex,
        startPosition = startPosition,
        endPosition = endPosition,
        highlightedText = highlightedText,
        color = color,
        dateCreated = dateCreated
    )
}
