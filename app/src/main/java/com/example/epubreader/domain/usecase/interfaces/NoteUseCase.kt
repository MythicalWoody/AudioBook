package com.example.epubreader.domain.usecase.interfaces

import com.example.epubreader.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteUseCase {
    fun getNotesForBook(bookId: String): Flow<List<Note>>
    fun getNotesForChapter(bookId: String, chapterIndex: Int): Flow<List<Note>>
    suspend fun addNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
}
