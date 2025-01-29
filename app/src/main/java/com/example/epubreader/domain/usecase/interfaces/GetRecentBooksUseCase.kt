package com.example.epubreader.domain.usecase.interfaces

import com.example.epubreader.domain.model.EpubBooks
import kotlinx.coroutines.flow.Flow

interface GetRecentBooksUseCase : NoParamsUseCase<Flow<List<EpubBooks>>>
