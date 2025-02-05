package com.example.epubreader.di

import android.content.Context
import androidx.room.Room
import com.example.epubreader.data.db.EpubReaderDatabase
import com.example.epubreader.data.db.dao.BookDao
import com.example.epubreader.data.db.dao.ChapterDao
import com.example.epubreader.data.db.dao.BookmarkDao
import com.example.epubreader.data.db.dao.HighlightDao
import com.example.epubreader.data.db.dao.NoteDao
import com.example.epubreader.data.db.dao.ReadingHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): EpubReaderDatabase {
        return Room.databaseBuilder(
            context,
            EpubReaderDatabase::class.java,
            "epub_reader_db"
        )
            .addMigrations(EpubReaderDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideBookDao(database: EpubReaderDatabase): BookDao {
        return database.getBookDao()
    }

    @Provides
    @Singleton
    fun provideChapterDao(database: EpubReaderDatabase): ChapterDao {
        return database.getChapterDao()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(database: EpubReaderDatabase): BookmarkDao {
        return database.getBookmarkDao()
    }

    @Provides
    @Singleton
    fun provideReadingHistoryDao(database: EpubReaderDatabase): ReadingHistoryDao {
        return database.getReadingHistoryDao()
    }

    @Provides
    @Singleton
    fun provideHighlightDao(database: EpubReaderDatabase): HighlightDao {
        return database.getHighlightDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: EpubReaderDatabase): NoteDao {
        return database.getNoteDao()
    }
}