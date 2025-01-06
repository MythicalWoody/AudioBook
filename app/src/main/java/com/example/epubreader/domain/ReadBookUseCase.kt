package com.example.epubreader.domain

import com.example.epubreader.domain.model.EpubBooks
import kotlinx.coroutines.flow.Flow

interface ReadBookUseCase {
    suspend fun execute(bookId: String): Flow<EpubBooks>
}