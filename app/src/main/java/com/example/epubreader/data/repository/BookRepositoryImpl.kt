package com.example.epubreader.data.repository

import android.content.Context
import android.net.Uri
import com.example.epubreader.common.exceptions.BookNotFoundException
import com.example.epubreader.data.db.dao.BookDao
import com.example.epubreader.data.db.dao.BookmarkDao
import com.example.epubreader.data.db.dao.ChapterDao
import com.example.epubreader.data.db.dao.ReadingHistoryDao
import com.example.epubreader.data.db.entity.BookEntity
import com.example.epubreader.data.db.entity.BookmarkEntity
import com.example.epubreader.data.db.entity.ChapterEntity
import com.example.epubreader.data.db.entity.ReadingHistoryEntity
import com.example.epubreader.data.mapper.BookMapper
import com.example.epubreader.data.utils.EpubParser
import com.example.epubreader.domain.model.Bookmark
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.model.ReadingPosition
import com.example.epubreader.domain.model.ReadingStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val bookmarkDao: BookmarkDao,
    private val readingHistoryDao: ReadingHistoryDao,
    private val epubParser: EpubParser,
    private val bookMapper: BookMapper
) : BookRepository {
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

    override suspend fun importBook(uri: Uri , context: Context): String = withContext(Dispatchers.IO) {
        val book = epubParser.parseBook(uri , context)
        val bookId = UUID.randomUUID().toString()

        // Save book metadata
        bookDao.insertBook(
            BookEntity(
            id = bookId,
            title = book.title,
            filePath = uri.toString()
        )
        )

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
        chapterDao.insertChapters(chapterEntities)

        return@withContext bookId
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

    suspend fun deleteBook(bookId: String) = withContext(Dispatchers.IO) {
        bookDao.getBook(bookId)?.let { book ->
            bookDao.deleteBook(book)
            chapterDao.deleteChaptersForBook(bookId)
            bookmarkDao.deleteBookmarksForBook(bookId)
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