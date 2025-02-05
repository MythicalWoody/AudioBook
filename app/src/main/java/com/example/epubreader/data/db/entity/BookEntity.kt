package com.example.epubreader.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.epubreader.data.model.ReadingPreferences
import com.example.epubreader.data.model.TextAlignment
import com.example.epubreader.data.model.Theme

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["title"]),
        Index(value = ["lastReadDate"]),
        Index(value = ["dateAdded"])
    ]
)
data class BookEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val filePath: String,
    val lastReadChapterIndex: Int = 0,
    val lastReadPosition: Int = 0,
    val readingPreferences: ReadingPreferences = ReadingPreferences(
        theme = Theme.LIGHT,
        textAlignment = TextAlignment.JUSTIFY
    ),
    val dateAdded: Long = System.currentTimeMillis(),
    val lastReadDate: Long = System.currentTimeMillis(),
    val coverImage: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BookEntity

        if (id != other.id) return false
        if (title != other.title) return false
        if (filePath != other.filePath) return false
        if (lastReadChapterIndex != other.lastReadChapterIndex) return false
        if (lastReadPosition != other.lastReadPosition) return false
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
        result = 31 * result + filePath.hashCode()
        result = 31 * result + lastReadChapterIndex
        result = 31 * result + lastReadPosition
        result = 31 * result + readingPreferences.hashCode()
        result = 31 * result + dateAdded.hashCode()
        result = 31 * result + lastReadDate.hashCode()
        result = 31 * result + (coverImage?.contentHashCode() ?: 0)
        return result
    }
}
