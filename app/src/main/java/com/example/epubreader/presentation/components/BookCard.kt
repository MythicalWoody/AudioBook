package com.example.epubreader.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.epubreader.domain.model.EpubBooks
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookCard(
    book: EpubBooks,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reading progress
                LinearProgressIndicator(
                    progress = calculateProgress(book),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                )
                
                // Last read date
                Text(
                    text = formatDate(book.lastReadDate),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun calculateProgress(book: EpubBooks): Float {
    return (book.currentPosition.chapterIndex.toFloat() / book.chapters.size.toFloat())
        .coerceIn(0f, 1f)
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}
