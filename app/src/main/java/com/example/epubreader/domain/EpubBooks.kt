package com.example.epubreader.domain

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class EpubBooks(
    val id: String,
    val title: String,
    val chapters: List<Chapter>,
    val currentPosition: ReadingPosition
) : Parcelable
