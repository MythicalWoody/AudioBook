package com.example.epubreader.data.mapper

import com.example.epubreader.data.db.entity.NoteEntity
import com.example.epubreader.domain.model.Note

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        bookId = bookId,
        chapterIndex = chapterIndex,
        position = position,
        content = content,
        dateCreated = dateCreated,
        dateModified = dateModified
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        bookId = bookId,
        chapterIndex = chapterIndex,
        position = position,
        content = content,
        dateCreated = dateCreated,
        dateModified = dateModified
    )
}
