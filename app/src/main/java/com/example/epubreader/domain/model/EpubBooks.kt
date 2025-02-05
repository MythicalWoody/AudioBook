package com.example.epubreader.domain.model

import androidx.annotation.Keep
import com.example.epubreader.data.model.ReadingPreferences
import java.util.Date

@Keep
data class EpubBooks(
    val id: String,
    val title: String,
    val chapters: List<Chapter>,
    val currentPosition: ReadingPosition,
    val readingPreferences: ReadingPreferences,
    val dateAdded: Date,
    val lastReadDate: Date,
    val coverImage: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EpubBooks

        if (id != other.id) return false
        if (title != other.title) return false
        if (chapters != other.chapters) return false
        if (currentPosition != other.currentPosition) return false
        if (readingPreferences != other.readingPreferences) return false
        if (dateAdded != other.dateAdded) return false
        if (lastReadDate != other.lastReadDate) return false
        if (coverImage != null) {
            if (other.coverImage == null) return false
            if (!coverImage.contentEquals(other.coverImage)) return false
        } else if (other.coverImage != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + chapters.hashCode()
        result = 31 * result + currentPosition.hashCode()
        result = 31 * result + readingPreferences.hashCode()
        result = 31 * result + dateAdded.hashCode()
        result = 31 * result + lastReadDate.hashCode()
        result = 31 * result + (coverImage?.contentHashCode() ?: 0)
        return result
    }
}
