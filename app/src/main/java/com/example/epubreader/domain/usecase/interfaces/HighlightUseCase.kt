package com.example.epubreader.domain.usecase.interfaces

import com.example.epubreader.domain.model.Highlight
import kotlinx.coroutines.flow.Flow

interface HighlightUseCase {
    fun getHighlightsForBook(bookId: String): Flow<List<Highlight>>
    fun getHighlightsForChapter(bookId: String, chapterIndex: Int): Flow<List<Highlight>>
    suspend fun addHighlight(highlight: Highlight)
    suspend fun deleteHighlight(highlight: Highlight)
}
