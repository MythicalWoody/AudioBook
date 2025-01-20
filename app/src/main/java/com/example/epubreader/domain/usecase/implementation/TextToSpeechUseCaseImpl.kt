package com.example.epubreader.domain.usecase.implementation

import com.example.epubreader.common.exceptions.BookReaderException
import com.example.epubreader.domain.usecase.interfaces.TextToSpeechUseCase
import com.example.epubreader.presentation.ui.reader.components.TextToSpeechController
import javax.inject.Inject

class TextToSpeechUseCaseImpl @Inject constructor(
    private val textToSpeechController: TextToSpeechController
) : TextToSpeechUseCase {
    override suspend fun speak(text: String, speechRate: Float): Result<Unit> =
        try {
            textToSpeechController.setSpeed(speechRate)
            textToSpeechController.speak(text)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(BookReaderException.TTSError("Failed to start TTS: ${e.message}"))
        }

    override suspend fun pause(): Result<Unit> =
        try {
            textToSpeechController.stop()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(BookReaderException.TTSError("Failed to pause TTS: ${e.message}"))
        }

    override suspend fun resume(): Result<Unit> =
        try {
            textToSpeechController.speak("")  // Resume from last position
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(BookReaderException.TTSError("Failed to resume TTS: ${e.message}"))
        }

    override suspend fun stop(): Result<Unit> =
        try {
            textToSpeechController.stop()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(BookReaderException.TTSError("Failed to stop TTS: ${e.message}"))
        }
}