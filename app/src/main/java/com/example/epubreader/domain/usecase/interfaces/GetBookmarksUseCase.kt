package com.example.epubreader.domain.usecase.interfaces

import com.example.epubreader.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface GetBookmarksUseCase : UseCase<String, Flow<List<Bookmark>>>