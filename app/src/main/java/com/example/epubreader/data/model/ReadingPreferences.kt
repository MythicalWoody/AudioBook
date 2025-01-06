package com.example.epubreader.data.model

import android.content.res.Resources
import android.content.res.Resources.Theme
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.compose.ui.text.style.TextAlign
import kotlinx.android.parcel.Parcelize

@Keep
data class ReadingPreferences(
    val fontSize: Int = 16,
    val fontFamily: String = "default",
    val lineSpacing: Float = 1.5f,
    val textAlignment: TextAlign = TextAlign.Justify,
    val theme: Theme = Resources.getSystem().newTheme().apply { applyStyle(android.R.style.Theme_Material_Light, true) }
)
