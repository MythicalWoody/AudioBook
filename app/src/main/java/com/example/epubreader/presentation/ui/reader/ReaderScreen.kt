package com.example.epubreader.presentation.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.epubreader.R
import com.example.epubreader.data.model.ReadingPreferences
import com.example.epubreader.data.model.TextAlignment
import com.example.epubreader.data.model.Theme
import com.example.epubreader.domain.model.Chapter
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.model.ReadingPosition
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    var showControls by remember { mutableStateOf(true) }
    var showChapterList by remember { mutableStateOf(false) }
    
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ReaderUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ReaderUiState.Success -> {
            Box(modifier = modifier.fillMaxSize()) {
                // Main content area with text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = if (showControls) 120.dp else 0.dp,
                            bottom = if (showControls) 64.dp else 0.dp
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    when {
                                        offset.x < size.width * 0.3f -> {
                                            // Previous page (area 9)
                                            // Handle previous page
                                        }
                                        offset.x > size.width * 0.7f -> {
                                            // Next page (area 10)
                                            // Handle next page
                                        }
                                        else -> {
                                            // Toggle controls
                                            showControls = !showControls
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    Text(
                        text = state.book.chapters.getOrNull(
                            state.book.currentPosition.chapterIndex
                        )?.content ?: "",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Top bar with controls (areas 1, 2, 3)
                AnimatedVisibility(
                    visible = showControls,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    TopAppBar(
                        navigationIcon = {
                            // Area 1: Burger menu
                            IconButton(onClick = { /* Show menu */ }) {
                                Icon(Icons.Default.Menu, "Menu")
                            }
                        },
                        title = {
                            // Area 2: Book title
                            Text(
                                text = state.book.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        actions = {
                            // Area 3: Settings
                            IconButton(onClick = { /* Show settings */ }) {
                                Icon(Icons.Default.MoreVert, "Settings")
                            }
                        }
                    )
                }

                // Bottom controls (areas 4-8)
                AnimatedVisibility(
                    visible = showControls,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Column {
                        // Area 5: Chapter name and navigation
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Area 6: Chapter list button
                            IconButton(onClick = { showChapterList = true }) {
                                Icon(Icons.Default.Menu, "Chapters")
                            }
                            
                            // Chapter name
                            Text(
                                text = state.book.chapters.getOrNull(
                                    state.book.currentPosition.chapterIndex
                                )?.title ?: "",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                        }

                        // Playback controls row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Area 7: Voice selection
                            IconButton(onClick = { /* Show voice options */ }) {
                                Icon(Icons.Default.Face, "Voice")
                            }

                            // Area 4: Playback controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { /* Skip backward */ }) {
                                    Icon(Icons.Default.Refresh, "Skip back 10 seconds",modifier = Modifier.scale(scaleX = -1f, scaleY = 1f))
                                }
                                IconButton(onClick = { /* Play/Pause */ }) {
                                    Icon(Icons.Default.PlayArrow, "Play/Pause")
                                }
                                IconButton(onClick = { /* Skip forward */ }) {
                                    Icon(Icons.Default.Refresh, "Skip forward 10 seconds")
                                }
                            }

                            // Area 8: Speed control
                            IconButton(onClick = { /* Show speed options */ }) {
                                Icon(painter = painterResource(R.drawable.speech_speed_ic), "Speed")
                            }
                        }
                    }
                }
            }

            // Chapter list dialog
            if (showChapterList) {
                AlertDialog(
                    onDismissRequest = { showChapterList = false },
                    title = { Text("Chapters") },
                    text = {
                        LazyColumn {
                            items(
                                items = state.book.chapters,
                                key = { chapter -> chapter.id }
                            ) { chapter ->
                                Text(
                                    text = chapter.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.updateReadingPosition(
                                                ReadingPosition(
                                                    state.book.chapters.indexOf(chapter),
                                                    0
                                                )
                                            )
                                            showChapterList = false
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showChapterList = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
        is ReaderUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(state.message ?: "Unknown error occurred")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReaderScreenContentPreview() {
    val sampleChapters = listOf(
        Chapter(
            id = "1",
            title = "Chapter 1: The Beginning",
            content = "Once upon a time..."
        ),
        Chapter(
            id = "2",
            title = "Chapter 2: The Journey",
            content = "The adventure begins..."
        ),
        Chapter(
            id = "3",
            title = "Chapter 3: The Climax",
            content = "At the peak of..."
        )
    )
    
    val sampleBook = EpubBooks(
        id = "preview_book",
        title = "Sample Book",
        chapters = sampleChapters,
        currentPosition = ReadingPosition(0, 0),
        readingPreferences = ReadingPreferences(
            fontSize = 16f,
            lineSpacing = 1.5f,
            fontFamily = "Default",
            theme = Theme.DARK,
            textAlignment = TextAlignment.JUSTIFY
        ),
        dateAdded = Date(),
        lastReadDate = Date()
    )

    var showControls by remember { mutableStateOf(true) }
    var showChapterList by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content area with text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (showControls) 120.dp else 35.dp,
                    bottom = if (showControls) 120.dp else 10.dp
                )
                .pointerInput(Unit) {
                    detectTapGestures (
                        onTap = { offset ->
                            when {
                                offset.x < size.width * 0.3f -> {
                                    // Previous page (area 9)
                                    // Handle previous page
                                }
                                offset.x > size.width * 0.7f -> {
                                    // Next page (area 10)
                                    // Handle next page
                                }
                                else -> {
                                    // Toggle controls
                                    showControls = !showControls
                                }
                            }
                        }
                    )
                }
        ) {
            Text(
                text = sampleChapters[0].content,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Top bar with controls
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Menu, "Menu")
                    }
                },
                title = {
                    Text(
                        text = sampleBook.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, "Settings")
                    }
                }
            )
        }

        // Bottom controls
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showChapterList = true }) {
                        Icon(Icons.Default.Menu, "Chapters")
                    }
                    
                    Text(
                        text = sampleChapters[0].title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Face, "Voice")
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Refresh, "Skip back 10 seconds", 
                                modifier = Modifier.scale(scaleX = -1f, scaleY = 1f))
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.PlayArrow, "Play/Pause")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Refresh, "Skip forward 10 seconds")
                        }
                    }

                    IconButton(onClick = { }) {
                        Icon(painter = painterResource(R.drawable.speech_speed_ic), "Speed")
                    }
                }
            }
        }

        if (showChapterList) {
            AlertDialog(
                onDismissRequest = { showChapterList = false },
                title = { Text("Chapters") },
                text = {
                    LazyColumn {
                        items(
                            items = sampleChapters,
                            key = { chapter -> chapter.id }
                        ) { chapter ->
                            Text(
                                text = chapter.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showChapterList = false
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChapterList = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
