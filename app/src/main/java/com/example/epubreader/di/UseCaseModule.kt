package com.example.epubreader.di

import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.implementation.AddBookmarkUseCaseImpl
import com.example.epubreader.domain.usecase.implementation.GetAllBooksUseCaseImpl
import com.example.epubreader.domain.usecase.implementation.GetBookmarksUseCaseImpl
import com.example.epubreader.domain.usecase.implementation.GetReadingStatisticsUseCaseImpl
import com.example.epubreader.domain.usecase.implementation.GetRecentBooksUseCaseImpl
import com.example.epubreader.domain.usecase.implementation.ImportBookUseCaseImpl
import com.example.epubreader.domain.usecase.implementation.ReadBookUseCaseImpl
import com.example.epubreader.domain.usecase.implementation.SearchBooksUseCaseImpl
import com.example.epubreader.domain.usecase.implementation.TextToSpeechUseCaseImpl
import com.example.epubreader.domain.usecase.implementation.UpdateReadingPositionUseCaseImpl
import com.example.epubreader.domain.usecase.interfaces.AddBookmarkUseCase
import com.example.epubreader.domain.usecase.interfaces.GetAllBooksUseCase
import com.example.epubreader.domain.usecase.interfaces.GetBookmarksUseCase
import com.example.epubreader.domain.usecase.interfaces.GetReadingStatisticsUseCase
import com.example.epubreader.domain.usecase.interfaces.GetRecentBooksUseCase
import com.example.epubreader.domain.usecase.interfaces.ImportBookUseCase
import com.example.epubreader.domain.usecase.interfaces.ReadBookUseCase
import com.example.epubreader.domain.usecase.interfaces.SearchBooksUseCase
import com.example.epubreader.domain.usecase.interfaces.TextToSpeechUseCase
import com.example.epubreader.domain.usecase.interfaces.UpdateReadingPositionUseCase
import com.example.epubreader.presentation.ui.reader.components.TextToSpeechController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideReadBookUseCase(
        bookRepository: BookRepository
    ): ReadBookUseCase = ReadBookUseCaseImpl(bookRepository)

    @Provides
    @Singleton
    fun provideUpdateReadingPositionUseCase(
        bookRepository: BookRepository
    ): UpdateReadingPositionUseCase = UpdateReadingPositionUseCaseImpl(bookRepository)

    @Provides
    @Singleton
    fun provideGetBookmarksUseCase(
        bookRepository: BookRepository
    ): GetBookmarksUseCase = GetBookmarksUseCaseImpl(bookRepository)

    @Provides
    @Singleton
    fun provideAddBookmarkUseCase(
        bookRepository: BookRepository
    ): AddBookmarkUseCase = AddBookmarkUseCaseImpl(bookRepository)

    @Provides
    @Singleton
    fun provideGetReadingStatisticsUseCase(
        bookRepository: BookRepository
    ): GetReadingStatisticsUseCase = GetReadingStatisticsUseCaseImpl(bookRepository)

    @Provides
    @Singleton
    fun provideSearchBooksUseCase(
        bookRepository: BookRepository
    ): SearchBooksUseCase = SearchBooksUseCaseImpl(bookRepository)

    @Provides
    @Singleton
    fun provideGetAllBooksUseCase(
        bookRepository: BookRepository
    ): GetAllBooksUseCase = GetAllBooksUseCaseImpl(bookRepository)

    @Provides
    @Singleton
    fun provideGetRecentBooksUseCase(
        bookRepository: BookRepository
    ): GetRecentBooksUseCase = GetRecentBooksUseCaseImpl(bookRepository)

    @Provides
    @Singleton
    fun provideImportBookUseCase(
        bookRepository: BookRepository
    ): ImportBookUseCase = ImportBookUseCaseImpl(bookRepository)

    @Provides
    @Singleton
    fun provideTextToSpeechUseCase(
        textToSpeechController: TextToSpeechController
    ): TextToSpeechUseCase = TextToSpeechUseCaseImpl(textToSpeechController)

    @Provides
    @Named("recentBooksLimit")
    fun provideRecentBooksLimit(): Int = 10
}