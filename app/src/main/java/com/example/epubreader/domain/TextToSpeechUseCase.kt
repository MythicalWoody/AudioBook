package com.example.epubreader.domain

interface TextToSpeechUseCase {
    suspend fun speak(text: String, speechRate: Float): Result<Unit>
    suspend fun pause(): Result<Unit>
    suspend fun resume(): Result<Unit>
    suspend fun stop(): Result<Unit>
}