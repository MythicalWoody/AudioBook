package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.common.exceptions.BookReaderException
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.ReadBookUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReadBookUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : ReadBookUseCase {
    override suspend fun execute(bookId: String): Flow<EpubBooks> =
        flow {
            emit(bookRepository.getBook(bookId))
        }.catch { error ->
                throw BookReaderException.ReadBookError("Failed to read book: ${error.message}")
            }
}