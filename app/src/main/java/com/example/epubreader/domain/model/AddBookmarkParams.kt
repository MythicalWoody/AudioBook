package com.example.epubreader.domain.model

data class AddBookmarkParams(
    val bookId: String,
    val position: ReadingPosition,
    val note: String? = null
)
