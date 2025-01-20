package com.example.epubreader.domain.model

import android.content.Context
import android.net.Uri

data class ImportBookParams(
    val uri: Uri,
    val context: Context
)
