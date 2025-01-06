package com.example.epubreader.domain.repository

import android.content.Context
import android.net.Uri
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.model.ReadingPosition

interface BookRepository {
    suspend fun getBook(bookId: String): EpubBooks
    suspend fun saveReadingPosition(bookId: String, position: ReadingPosition)
    suspend fun importBook(uri: Uri, context: Context): String
}