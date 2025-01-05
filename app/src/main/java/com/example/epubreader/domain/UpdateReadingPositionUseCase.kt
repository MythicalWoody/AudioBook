package com.example.epubreader.domain

interface UpdateReadingPositionUseCase {
    suspend fun execute(bookId: String, position: ReadingPosition)
}