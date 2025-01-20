package com.example.epubreader.common.exceptions

sealed class BookReaderException(message: String) : Exception(message) {
    class ReadBookError(message: String) : BookReaderException(message)
    class SaveProgressError(message: String) : BookReaderException(message)
    class BookmarkError(message: String) : BookReaderException(message)
    class StatisticsError(message: String) : BookReaderException(message)
    class SearchError(message: String) : BookReaderException(message)
    class GetBooksError(message: String) : BookReaderException(message)
    class ImportError(message: String) : BookReaderException(message)
    class TTSError(message: String) : BookReaderException(message)
}