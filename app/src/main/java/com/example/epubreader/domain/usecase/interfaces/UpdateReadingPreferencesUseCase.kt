package com.example.epubreader.domain.usecase.interfaces

import com.example.epubreader.data.model.ReadingPreferences

interface UpdateReadingPreferencesUseCase {
    suspend fun execute(bookId: String, preferences: ReadingPreferences)
}
