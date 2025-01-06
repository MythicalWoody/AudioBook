package com.example.epubreader.domain

import com.example.epubreader.domain.model.ReadingPosition

interface UpdateReadingPositionUseCase {
    suspend fun execute(bookId: String, position: ReadingPosition)
}