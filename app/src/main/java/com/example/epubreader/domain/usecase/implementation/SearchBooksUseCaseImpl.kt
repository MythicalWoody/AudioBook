package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.common.exceptions.BookReaderException
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.SearchBooksUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class SearchBooksUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : SearchBooksUseCase {
    override suspend fun execute(query: String): Flow<List<EpubBooks>> =
        bookRepository.searchBooks(query)
            .catch { error ->
                throw BookReaderException.SearchError("Failed to search books: ${error.message}")
            }
}