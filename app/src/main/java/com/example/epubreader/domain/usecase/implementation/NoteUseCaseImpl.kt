package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.domain.model.Note
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.NoteUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : NoteUseCase {
    override fun getNotesForBook(bookId: String): Flow<List<Note>> {
        return bookRepository.getNotesForBook(bookId)
    }

    override fun getNotesForChapter(bookId: String, chapterIndex: Int): Flow<List<Note>> {
        return bookRepository.getNotesForChapter(bookId, chapterIndex)
    }

    override suspend fun addNote(note: Note) {
        bookRepository.addNote(note)
    }

    override suspend fun updateNote(note: Note) {
        bookRepository.updateNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        bookRepository.deleteNote(note)
    }
}
