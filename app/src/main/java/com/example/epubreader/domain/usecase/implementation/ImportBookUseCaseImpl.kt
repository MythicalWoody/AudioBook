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
            android.util.Log.d("ImportBookUseCase", "Starting book import process with URI: ${params.uri}")
            val bookId = bookRepository.importBook(params.uri, params.context)
            android.util.Log.d("ImportBookUseCase", "Successfully imported book with ID: $bookId")
            bookId
        } catch (e: Exception) {
            android.util.Log.e("ImportBookUseCase", "Failed to import book", e)
            throw BookReaderException.ImportError("Failed to import book: ${e.message}")
        }
}