package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.common.exceptions.BookReaderException
import com.example.epubreader.domain.model.AddBookmarkParams
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.AddBookmarkUseCase
import javax.inject.Inject

class AddBookmarkUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : AddBookmarkUseCase {
    override suspend fun execute(params: AddBookmarkParams) {
        try {
            params.note?.let {
                bookRepository.addBookmark(
                    bookId = params.bookId,
                    position = params.position,
                    note = it
                )
            }
        } catch (e: Exception) {
            throw BookReaderException.BookmarkError("Failed to add bookmark: ${e.message}")
        }
    }
}