package com.example.epubreader.domain.usecase.interfaces

interface TextToSpeechUseCase {
    suspend fun speak(text: String, speechRate: Float): Result<Unit>
    suspend fun pause(): Result<Unit>
    suspend fun resume(): Result<Unit>
    suspend fun stop(): Result<Unit>
}