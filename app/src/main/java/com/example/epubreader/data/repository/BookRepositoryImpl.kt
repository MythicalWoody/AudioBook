package com.example.epubreader.data.repository

import android.content.Context
import android.net.Uri
import com.example.epubreader.common.exceptions.BookNotFoundException
import com.example.epubreader.data.db.dao.BookDao
import com.example.epubreader.data.db.dao.BookmarkDao
import com.example.epubreader.data.db.dao.ChapterDao
import com.example.epubreader.data.db.entity.BookEntity
import com.example.epubreader.data.db.entity.BookmarkEntity
import com.example.epubreader.data.db.entity.ChapterEntity
import com.example.epubreader.data.mapper.BookMapper
import com.example.epubreader.data.utils.EpubParser
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.model.ReadingPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val bookmarkDao: BookmarkDao,
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

    suspend fun addBookmark(bookId: String, position: ReadingPosition, note: String? = null) =
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

    fun getBookmarks(bookId: String): Flow<List<BookmarkEntity>> =
        bookmarkDao.getBookmarksForBook(bookId)

    suspend fun deleteBook(bookId: String) = withContext(Dispatchers.IO) {
        bookDao.getBook(bookId)?.let { book ->
            bookDao.deleteBook(book)
            chapterDao.deleteChaptersForBook(bookId)
            bookmarkDao.deleteBookmarksForBook(bookId)
        }
    }
}