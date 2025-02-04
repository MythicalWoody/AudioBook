package com.example.epubreader.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.epubreader.data.db.entity.BookEntity
import com.example.epubreader.data.model.BookReadingStats
import com.example.epubreader.data.model.BookWithChapterCount
import com.example.epubreader.data.model.BookWithProgress
import com.example.epubreader.data.model.DailyReadingStats
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    // Simplified sorting using direct column reference (safer in Room)
    @Query("""
        SELECT b.*, COUNT(c.id) as chapterCount 
        FROM books b 
        LEFT JOIN chapters c ON b.id = c.bookId 
        GROUP BY b.id 
        ORDER BY 
            CASE :sortBy 
                WHEN 'title' THEN b.title 
                WHEN 'dateAdded' THEN b.dateAdded 
                ELSE b.lastReadDate 
            END DESC
    """)
    fun getBooksSorted(sortBy: String): Flow<List<BookWithChapterCount>>

    // Simplified search using JOIN instead of EXISTS
    @Query("""
        SELECT DISTINCT b.* 
        FROM books b 
        LEFT JOIN chapters c ON b.id = c.bookId
        WHERE b.title LIKE '%' || :query || '%' 
           OR c.content LIKE '%' || :query || '%'
    """)
    suspend fun searchBooks(query: String): List<BookEntity>

    // Simplified reading stats with date calculation fix
    @Query("""
        SELECT 
            b.id,
            b.title,
            COUNT(DISTINCT DATE(bh.timestamp/1000, 'unixepoch')) as daysRead,
            SUM(bh.duration) as totalReadingTime
        FROM books b
        LEFT JOIN book_reading_history bh ON b.id = bh.bookId
        WHERE bh.timestamp >= :startTime
        GROUP BY b.id
    """)
    fun getReadingStats(startTime: Long): Flow<List<BookReadingStats>>

    // Fixed query with proper cutoff date usage
    @Transaction
    @Query("""
        SELECT b.*, 
            (b.lastReadChapterIndex * 1.0 / MAX(c.totalChapters)) as readingProgress
        FROM books b
        LEFT JOIN (
            SELECT bookId, COUNT(*) as totalChapters 
            FROM chapters 
            GROUP BY bookId
        ) c ON b.id = c.bookId
        WHERE b.lastReadDate >= :cutoffDate
        ORDER BY b.lastReadDate DESC
        LIMIT :limit
    """)
    fun getRecentlyReadBooks(cutoffDate: Long, limit: Int = 10): Flow<List<BookWithProgress>>

    // Basic CRUD operations remain unchanged
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBook(bookId: String): BookEntity?

    @Query("SELECT * FROM books ORDER BY lastReadDate DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("""
        UPDATE books 
        SET 
            lastReadChapterIndex = :chapterIndex,
            lastReadPosition = :position,
            lastReadDate = :timestamp 
        WHERE id = :bookId
    """)
    suspend fun updateReadingPosition(bookId: String, chapterIndex: Int, position: Int, timestamp: Long = System.currentTimeMillis())
}