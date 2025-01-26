package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.DeleteBookUseCase
import javax.inject.Inject

class DeleteBookUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : DeleteBookUseCase {
    override suspend fun execute(bookId: String) {
        bookRepository.deleteBook(bookId)
    }
}
