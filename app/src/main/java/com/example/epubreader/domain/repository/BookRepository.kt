package com.example.epubreader.domain.repository

import android.content.Context
import android.net.Uri
import com.example.epubreader.data.model.ReadingPreferences
import com.example.epubreader.domain.model.Bookmark
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.model.Highlight
import com.example.epubreader.domain.model.Note
import com.example.epubreader.domain.model.ReadingPosition
import com.example.epubreader.domain.model.ReadingStatistics
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    // Book operations
    suspend fun getBook(bookId: String): EpubBooks
    suspend fun getAllBooks(): Flow<List<EpubBooks>>
    suspend fun importBook(uri: Uri, context: Context): String
    suspend fun deleteBook(bookId: String)
    fun searchBooks(query: String): Flow<List<EpubBooks>>
    fun getRecentBooks(limit: Int): Flow<List<EpubBooks>>

    // Reading position and preferences
    suspend fun saveReadingPosition(bookId: String, position: ReadingPosition)
    suspend fun updateReadingPreferences(bookId: String, preferences: ReadingPreferences)

    // Bookmarks
    suspend fun getBookmarks(bookId: String): List<Bookmark>
    suspend fun addBookmark(bookId: String, position: ReadingPosition, note: String)
    suspend fun deleteBookmark(bookId: String, bookmarkId: String)

    // Highlights
    fun getHighlightsForBook(bookId: String): Flow<List<Highlight>>
    fun getHighlightsForChapter(bookId: String, chapterIndex: Int): Flow<List<Highlight>>
    suspend fun addHighlight(highlight: Highlight)
    suspend fun deleteHighlight(highlight: Highlight)

    // Notes
    fun getNotesForBook(bookId: String): Flow<List<Note>>
    fun getNotesForChapter(bookId: String, chapterIndex: Int): Flow<List<Note>>
    suspend fun addNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)

    // Statistics
    suspend fun getReadingStatistics(bookId: String): ReadingStatistics {
        return ReadingStatistics(0, 0, 0)
    }
}