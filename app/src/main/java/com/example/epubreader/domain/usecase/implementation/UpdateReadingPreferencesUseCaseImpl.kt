package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.data.model.ReadingPreferences
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.UpdateReadingPreferencesUseCase
import javax.inject.Inject

class UpdateReadingPreferencesUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : UpdateReadingPreferencesUseCase {
    override suspend fun execute(bookId: String, preferences: ReadingPreferences) {
        bookRepository.updateReadingPreferences(bookId, preferences)
    }
}
