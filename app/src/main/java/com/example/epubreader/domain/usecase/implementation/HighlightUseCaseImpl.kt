package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.domain.model.Highlight
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.HighlightUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HighlightUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : HighlightUseCase {
    override fun getHighlightsForBook(bookId: String): Flow<List<Highlight>> {
        return bookRepository.getHighlightsForBook(bookId)
    }

    override fun getHighlightsForChapter(bookId: String, chapterIndex: Int): Flow<List<Highlight>> {
        return bookRepository.getHighlightsForChapter(bookId, chapterIndex)
    }

    override suspend fun addHighlight(highlight: Highlight) {
        bookRepository.addHighlight(highlight)
    }

    override suspend fun deleteHighlight(highlight: Highlight) {
        bookRepository.deleteHighlight(highlight)
    }
}
