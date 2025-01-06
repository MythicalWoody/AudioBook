package com.example.epubreader.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.epubreader.data.db.convertor.DateConverter
import com.example.epubreader.data.db.convertor.ReadingPreferencesConverter
import com.example.epubreader.data.db.dao.BookDao
import com.example.epubreader.data.db.dao.BookmarkDao
import com.example.epubreader.data.db.dao.ChapterDao
import com.example.epubreader.data.db.dao.ReadingHistoryDao
import com.example.epubreader.data.db.entity.BookEntity
import com.example.epubreader.data.db.entity.BookmarkEntity
import com.example.epubreader.data.db.entity.ChapterEntity
import com.example.epubreader.data.db.entity.ReadingHistoryEntity

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        BookmarkEntity::class,
        ReadingHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class, ReadingPreferencesConverter::class)
abstract class EpubReaderDatabase : RoomDatabase() {
    abstract fun getBookDao(): BookDao
    abstract fun getChapterDao(): ChapterDao
    abstract fun getBookmarkDao(): BookmarkDao
    abstract fun getReadingHistoryDao(): ReadingHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: EpubReaderDatabase? = null

        fun getDatabase(context: Context): EpubReaderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EpubReaderDatabase::class.java,
                    "epub_reader_db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}