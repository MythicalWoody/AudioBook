package com.example.epubreader.data.model

import androidx.annotation.Keep

@Keep
data class ReadingPreferences(
    val theme: Theme,
    val textAlignment: TextAlignment,
    val fontSize: Float = 16f,
    val fontFamily: String = "sans-serif",
    val lineSpacing: Float = 1.5f,
    val paragraphSpacing: Float = 2.0f,
    val marginSize: Float = 16f,
    val brightness: Float = 1.0f,
    val isNightMode: Boolean = false,
    val keepScreenOn: Boolean = true
)
