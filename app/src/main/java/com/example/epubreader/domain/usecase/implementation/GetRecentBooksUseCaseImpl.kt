package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.common.exceptions.BookReaderException
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.GetRecentBooksUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Named

class GetRecentBooksUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository,
    @Named("recentBooksLimit") private val limit: Int = 10
) : GetRecentBooksUseCase {
    override suspend fun execute(): Flow<List<EpubBooks>> =
        bookRepository.getRecentBooks(limit)
            .catch { error ->
                throw BookReaderException.GetBooksError("Failed to get recent books: ${error.message}")
            }
}