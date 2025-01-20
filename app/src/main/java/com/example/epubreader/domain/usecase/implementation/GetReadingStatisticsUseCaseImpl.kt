package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.common.exceptions.BookReaderException
import com.example.epubreader.domain.model.ReadingStatistics
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.domain.usecase.interfaces.GetReadingStatisticsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetReadingStatisticsUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetReadingStatisticsUseCase {
    override suspend fun execute(bookId: String): Flow<ReadingStatistics> =
        flow {
            emit(bookRepository.getReadingStatistics(bookId))
        }.catch { error ->
                throw BookReaderException.StatisticsError("Failed to get reading statistics: ${error.message}")
            }
}