package com.example.epubreader.domain.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.util.Date

@Keep
@Parcelize
data class Note(
    val id: String,
    val bookId: String,
    val chapterIndex: Int,
    val position: Int,
    val content: String,
    val dateCreated: Date,
    val dateModified: Date
) : Parcelable
