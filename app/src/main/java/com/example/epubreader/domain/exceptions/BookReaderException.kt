package com.example.epubreader.domain.exceptions

sealed class BookReaderException(message: String) : Exception(message) {
    class FileNotFound(message: String) : BookReaderException(message)
    class InvalidFile(message: String) : BookReaderException(message)
    class ReadBookError(message: String) : BookReaderException(message)
    class ImportBookError(message: String) : BookReaderException(message)
    class DeleteBookError(message: String) : BookReaderException(message)
    class DatabaseError(message: String) : BookReaderException(message)
    class NetworkError(message: String) : BookReaderException(message)
}
