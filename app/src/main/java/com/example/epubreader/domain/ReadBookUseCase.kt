package com.example.epubreader.domain

import kotlinx.coroutines.flow.Flow

interface ReadBookUseCase {
    suspend fun execute(bookId: String): Flow<EpubBooks>
}