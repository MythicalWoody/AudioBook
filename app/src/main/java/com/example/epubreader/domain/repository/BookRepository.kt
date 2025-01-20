package com.example.epubreader.domain.repository

import android.content.Context
import android.net.Uri
import com.example.epubreader.domain.model.Bookmark
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.model.ReadingPosition
import com.example.epubreader.domain.model.ReadingStatistics
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    suspend fun getBook(bookId: String): EpubBooks
    suspend fun saveReadingPosition(bookId: String, position: ReadingPosition)
    suspend fun importBook(uri: Uri, context: Context): String
    suspend fun getBookmarks(bookId: String): List<Bookmark>
    suspend fun addBookmark(bookId: String, position: ReadingPosition, note: String)
    suspend fun deleteBookmark(bookId: String, bookmarkId: String)
    suspend fun getReadingStatistics(bookId: String): ReadingStatistics {
        return ReadingStatistics(0, 0, 0)
    }
    
    fun searchBooks(query: String): Flow<List<EpubBooks>>
    fun getRecentBooks(limit: Int): Flow<List<EpubBooks>>
}