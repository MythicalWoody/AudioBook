package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.common.exceptions.BookReaderException
import com.example.epubreader.domain.model.Bookmark
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.GetBookmarksUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetBookmarksUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetBookmarksUseCase {
    override suspend fun execute(bookId: String): Flow<List<Bookmark>> =
        flow {
         emit(bookRepository.getBookmarks(bookId))
        }.catch { error ->
                throw BookReaderException.BookmarkError("Failed to get bookmarks: ${error.message}")
            }
}