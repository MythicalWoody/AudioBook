package com.example.epubreader.domain.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ReadingPosition(
    val chapterIndex: Int,
    val textPosition: Int
) : Parcelable
