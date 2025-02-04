package com.example.epubreader.presentation.ui.reader.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale
import javax.inject.Inject

class TextToSpeechControllerImpl @Inject constructor(
    context: Context
) : TextToSpeechController {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        textToSpeech = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS
        }
    }

    override fun speak(text: String) {
        if (isInitialized) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun stop() {
        textToSpeech?.stop()
    }

    override fun setSpeed(speed: Float) {
        textToSpeech?.setSpeechRate(speed)
    }

    override fun setLanguage(locale: Locale) {
        textToSpeech?.language = locale
    }

    override fun setVoice(voice: String) {
        textToSpeech?.voices?.find { it.name == voice }?.let { selectedVoice ->
            textToSpeech?.voice = selectedVoice
        }
    }

    fun release() {
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}