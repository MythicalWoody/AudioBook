package com.example.epubreader.domain.usecase.interfaces

import com.example.epubreader.domain.model.EpubBooks
import kotlinx.coroutines.flow.Flow

interface GetAllBooksUseCase {
    suspend fun execute(): Flow<List<EpubBooks>>
}