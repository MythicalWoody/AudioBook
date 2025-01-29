package com.example.epubreader.domain.model

data class Bookmark(
    val id: String,
    val bookId: String,
    val page: Int,
    val note: String? = null
)
