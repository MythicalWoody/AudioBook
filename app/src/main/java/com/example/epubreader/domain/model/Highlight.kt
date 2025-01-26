package com.example.epubreader.domain.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.util.Date

@Keep
@Parcelize
data class Highlight(
    val id: String,
    val bookId: String,
    val chapterIndex: Int,
    val startPosition: Int,
    val endPosition: Int,
    val highlightedText: String,
    val color: Int,
    val dateCreated: Date
) : Parcelable
