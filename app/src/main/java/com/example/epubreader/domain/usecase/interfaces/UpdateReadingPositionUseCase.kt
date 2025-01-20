package com.example.epubreader.domain.usecase.interfaces

import com.example.epubreader.domain.model.ReadingPosition

interface UpdateReadingPositionUseCase {
    suspend fun execute(bookId: String, position: ReadingPosition)
}