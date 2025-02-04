package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.GetAllBooksUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllBooksUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetAllBooksUseCase {
    override suspend fun execute(): Flow<List<EpubBooks>> =
        bookRepository.getAllBooks()
}