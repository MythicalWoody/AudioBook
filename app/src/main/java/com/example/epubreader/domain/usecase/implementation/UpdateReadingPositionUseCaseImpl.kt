package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.common.exceptions.BookReaderException
import com.example.epubreader.domain.model.ReadingPosition
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.UpdateReadingPositionUseCase
import javax.inject.Inject

class UpdateReadingPositionUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : UpdateReadingPositionUseCase {
    override suspend fun execute(bookId: String, position: ReadingPosition) {
        try {
            bookRepository.saveReadingPosition(bookId, position)
        } catch (e: Exception) {
            throw BookReaderException.SaveProgressError("Failed to save reading position: ${e.message}")
        }
    }
}