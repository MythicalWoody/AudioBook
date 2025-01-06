package com.example.epubreader.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.epubreader.data.db.entity.ReadingHistoryEntity
import com.example.epubreader.data.model.DailyReadingStats
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingHistoryDao {
    @Insert
    suspend fun insertReadingSession(session: ReadingHistoryEntity)

    @Query("""
        SELECT SUM(duration) 
        FROM book_reading_history 
        WHERE bookId = :bookId
    """)
    suspend fun getTotalReadingTime(bookId: String): Long?

    @Query("""
        SELECT AVG(readingSpeed) 
        FROM book_reading_history 
        WHERE bookId = :bookId 
        AND readingSpeed IS NOT NULL
    """)
    suspend fun getAverageReadingSpeed(bookId: String): Float?

    @Query("""
        SELECT * 
        FROM book_reading_history 
        WHERE bookId = :bookId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun getRecentReadingSessions(bookId: String, limit: Int = 10): Flow<List<ReadingHistoryEntity>>

    @Query("""
        SELECT 
            strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch') as date,
            SUM(duration) as totalDuration,
            SUM(pagesRead) as totalPages
        FROM book_reading_history
        WHERE timestamp >= :startTime
        GROUP BY date
        ORDER BY date DESC
    """)
    fun getReadingStatsByDay(startTime: Long): Flow<List<DailyReadingStats>>
}