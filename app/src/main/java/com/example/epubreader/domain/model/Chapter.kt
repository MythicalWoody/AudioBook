package com.example.epubreader.domain.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Chapter(
    val id: String,
    val title: String,
    val content: String
) : Parcelable
