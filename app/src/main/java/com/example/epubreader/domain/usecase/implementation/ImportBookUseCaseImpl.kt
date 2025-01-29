package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.common.exceptions.BookReaderException
import com.example.epubreader.domain.model.ImportBookParams
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.ImportBookUseCase
import javax.inject.Inject

class ImportBookUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : ImportBookUseCase {
    override suspend fun execute(params: ImportBookParams): String =
        try {
            bookRepository.importBook(params.uri, params.context)
        } catch (e: Exception) {
            throw BookReaderException.ImportError("Failed to import book: ${e.message}")
        }
}