package com.example.epubreader.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.epubreader.common.exceptions.BookNotFoundException
import com.example.epubreader.data.db.dao.*
import com.example.epubreader.data.db.entity.*
import com.example.epubreader.data.mapper.BookMapper
import com.example.epubreader.data.mapper.toDomain
import com.example.epubreader.data.mapper.toEntity
import com.example.epubreader.data.model.ReadingPreferences
import com.example.epubreader.data.utils.EpubParser
import com.example.epubreader.domain.model.*
import com.example.epubreader.domain.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val bookmarkDao: BookmarkDao,
    private val readingHistoryDao: ReadingHistoryDao,
    private val highlightDao: HighlightDao,
    private val noteDao: NoteDao,
    private val epubParser: EpubParser,
    private val bookMapper: BookMapper
) : BookRepository {

    override suspend fun getAllBooks(): Flow<List<EpubBooks>> {
        return bookDao.getAllBooks().map { books ->
            books.map { bookEntity ->
                val chapters = chapterDao.getChaptersForBook(bookEntity.id)
                bookMapper.mapToEntity(bookEntity, chapters)
            }
        }
    }

    override suspend fun getBook(bookId: String): EpubBooks =
        withContext(Dispatchers.IO) {
            val bookEntity = bookDao.getBook(bookId)
                ?: throw BookNotFoundException(bookId)
            val chapters = chapterDao.getChaptersForBook(bookId)
            bookMapper.mapToEntity(bookEntity, chapters)
        }

    override suspend fun saveReadingPosition(bookId: String, position: ReadingPosition) =
        withContext(Dispatchers.IO) {
            bookDao.updateReadingPosition(
                bookId = bookId,
                chapterIndex = position.chapterIndex,
                position = position.textPosition
            )
        }

    override suspend fun updateReadingPreferences(bookId: String, preferences: ReadingPreferences) =
        withContext(Dispatchers.IO) {
            val book = bookDao.getBook(bookId) ?: throw BookNotFoundException(bookId)
            bookDao.updateBook(book.copy(readingPreferences = preferences))
        }

    override suspend fun importBook(uri: Uri, context: Context): String = withContext(Dispatchers.IO) {
        Log.d("BookRepository1", "Starting book import in repository with URI: $uri")
        
        try {
            val book = epubParser.parseBook(uri, context)
            Log.d("BookRepository2", "Successfully parsed EPUB book: ${book.title}")
            Log.d("BookRepository3", "Parsed chapters: ${book.chapters.size} with content lengths: ${book.chapters.map { it.content.length }}")
            
            val bookId = UUID.randomUUID().toString()
            Log.d("BookRepository4", "Generated book ID: $bookId")

            // Save book metadata
            bookDao.insertBook(
                BookEntity(
                    id = bookId,
                    title = book.title,
                    filePath = uri.toString(),
                    coverImage = book.coverImage
                )
            )
            Log.d("BookRepository5", "Saved book metadata to database")

            // Save chapters
            val chapterEntities = book.chapters.mapIndexed { index, chapter ->
                ChapterEntity(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    title = chapter.title,
                    content = chapter.content,
                    indexInBook = index
                )
            }
            Log.d("BookRepository6", "Created ${chapterEntities.size} chapter entities with content lengths: ${chapterEntities.map { it.content.length }}")
            
            chapterDao.insertChapters(chapterEntities)
            Log.d("BookRepository7", "Successfully saved all chapters to database")

            return@withContext bookId
        } catch (e: Exception) {
            android.util.Log.e("BookRepository8", "Error importing book", e)
            throw e
        }
    }

    override suspend fun getBookmarks(bookId: String): List<Bookmark> =
        withContext(Dispatchers.IO) {
            bookmarkDao.getBookmarksForBook(bookId)
                .first() // Convert Flow to single List
                .map { bookmarkEntity ->
                    Bookmark(
                        id = bookmarkEntity.id.toString(),
                        bookId = bookmarkEntity.bookId,
                        page = bookmarkEntity.textPosition,
                        note = bookmarkEntity.note
                    )
                }
        }

    override suspend fun addBookmark(bookId: String, position: ReadingPosition, note: String) {
        withContext(Dispatchers.IO) {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    bookId = bookId,
                    chapterIndex = position.chapterIndex,
                    textPosition = position.textPosition,
                    note = note
                )
            )
        }
    }

    override suspend fun deleteBookmark(bookId: String, bookmarkId: String) {
        withContext(Dispatchers.IO) {
            bookmarkDao.getBookmarksForBook(bookId)
                .first()
                .find { it.id.toString() == bookmarkId }
                ?.let { bookmark ->
                    bookmarkDao.deleteBookmark(bookmark)
                }
        }
    }

    override suspend fun deleteBook(bookId: String) {
        withContext(Dispatchers.IO) {
            bookDao.getBook(bookId)?.let { book ->
                bookDao.deleteBook(book)
                chapterDao.deleteChaptersForBook(bookId)
                bookmarkDao.deleteBookmarksForBook(bookId)
            }
        }
    }

    override fun searchBooks(query: String): Flow<List<EpubBooks>> =
        bookDao.getBooksSorted("title")
            .map { books ->
                books.filter { book ->
                    book.book.title.contains(query, ignoreCase = true)
                }.map { bookWithChapterCount ->
                    val chapters = chapterDao.getChaptersForBook(bookWithChapterCount.book.id)
                    bookMapper.mapToEntity(bookWithChapterCount.book, chapters)
                }
            }

    override fun getRecentBooks(limit: Int): Flow<List<EpubBooks>> =
        bookDao.getRecentlyReadBooks(
            cutoffDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000), // Last 30 days
            limit = limit
        ).map { books ->
            books.map { bookWithProgress ->
                val chapters = chapterDao.getChaptersForBook(bookWithProgress.book.id)
                bookMapper.mapToEntity(bookWithProgress.book, chapters)
            }
        }

    override fun getHighlightsForBook(bookId: String): Flow<List<Highlight>> {
        return highlightDao.getHighlightsForBook(bookId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHighlightsForChapter(bookId: String, chapterIndex: Int): Flow<List<Highlight>> {
        return highlightDao.getHighlightsForChapter(bookId, chapterIndex).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addHighlight(highlight: Highlight) = withContext(Dispatchers.IO) {
        highlightDao.insertHighlight(highlight.toEntity())
    }

    override suspend fun deleteHighlight(highlight: Highlight) = withContext(Dispatchers.IO) {
        highlightDao.deleteHighlight(highlight.toEntity())
    }

    override fun getNotesForBook(bookId: String): Flow<List<Note>> {
        return noteDao.getNotesForBook(bookId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getNotesForChapter(bookId: String, chapterIndex: Int): Flow<List<Note>> {
        return noteDao.getNotesForChapter(bookId, chapterIndex).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note.toEntity())
    }

    override suspend fun getReadingStatistics(bookId: String): ReadingStatistics =
        withContext(Dispatchers.IO) {
            val totalReadingTime = readingHistoryDao.getTotalReadingTime(bookId) ?: 0L
            val totalPagesRead = readingHistoryDao.getTotalPagesRead(bookId) ?: 0
            val booksRead = readingHistoryDao.getCompletedBooksCount() ?: 0

            ReadingStatistics(
                totalBooksRead = booksRead,
                totalPagesRead = totalPagesRead,
                totalReadingTime = totalReadingTime
            )
        }

    suspend fun trackReadingSession(
        bookId: String,
        duration: Long,
        pagesRead: Int,
        readingSpeed: Float,
        chapterIndex: Int
    ) = withContext(Dispatchers.IO) {
        readingHistoryDao.insertReadingSession(
            ReadingHistoryEntity(
                bookId = bookId,
                timestamp = System.currentTimeMillis(),
                duration = duration,
                pagesRead = pagesRead,
                readingSpeed = readingSpeed,
                chapterIndex = chapterIndex
            )
        )
    }
}