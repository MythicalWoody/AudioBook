package com.example.epubreader.presentation.ui.reader.components

import java.util.Locale

interface TextToSpeechController {
    fun speak(text: String)
    fun stop()
    fun setSpeed(speed: Float)
    fun setLanguage(locale: Locale)
    fun setVoice(voice: String)
}