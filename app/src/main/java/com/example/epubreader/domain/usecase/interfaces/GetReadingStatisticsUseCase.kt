package com.example.epubreader.domain.usecase.interfaces

import com.example.epubreader.domain.model.ReadingStatistics
import kotlinx.coroutines.flow.Flow

interface GetReadingStatisticsUseCase : UseCase<String, Flow<ReadingStatistics>>