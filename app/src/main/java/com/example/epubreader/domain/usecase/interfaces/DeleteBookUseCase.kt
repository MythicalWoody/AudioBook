package com.example.epubreader.domain.usecase.interfaces

interface DeleteBookUseCase {
    suspend fun execute(bookId: String)
}
