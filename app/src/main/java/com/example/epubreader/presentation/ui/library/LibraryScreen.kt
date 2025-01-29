package com.example.epubreader.presentation.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.epubreader.data.model.ReadingPreferences
import com.example.epubreader.data.model.TextAlignment
import com.example.epubreader.data.model.Theme
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.model.ReadingPosition
import com.example.epubreader.domain.usecase.interfaces.GetRecentBooksUseCase
import com.example.epubreader.domain.usecase.interfaces.SearchBooksUseCase
import com.example.epubreader.presentation.components.BookCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBookClick: (String) -> Unit,
    onImportBook: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            SearchTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::searchBooks
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onImportBook) {
                Icon(Icons.Default.Add, contentDescription = "Import Book")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is LibraryUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is LibraryUiState.Success -> {
                if (state.books.isEmpty()) {
                    EmptyLibrary(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = paddingValues,
                        modifier = modifier.fillMaxSize()
                    ) {
                        items(state.books) { book ->
                            BookCard(
                                book = book,
                                onClick = { onBookClick(book.id) },
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
            is LibraryUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = viewModel::loadBooks,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    TopAppBar(
        title = { Text("Library") },
        actions = {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search books...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                singleLine = true
            )
        }
    )
}

@Composable
private fun EmptyLibrary(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your library is empty",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Import your first book to get started",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    val previewViewModel = object : LibraryViewModel(
        object : GetRecentBooksUseCase {
            override suspend fun execute() = MutableStateFlow(
                listOf(
                    EpubBooks(
                        id = "1",
                        title = "Sample Book 1",
                        chapters = listOf(),
                        currentPosition = ReadingPosition(0, 0),
                        readingPreferences = ReadingPreferences(Theme.LIGHT, TextAlignment.JUSTIFY),
                        dateAdded = Date(),
                        lastReadDate = Date()
                    ),
                    EpubBooks(
                        id = "2",
                        title = "Sample Book 2",
                        chapters = listOf(),
                        currentPosition = ReadingPosition(1, 50),
                        readingPreferences = ReadingPreferences(Theme.LIGHT, TextAlignment.JUSTIFY),
                        dateAdded = Date(),
                        lastReadDate = Date()
                    )
                )
            ).asStateFlow()
        },
        object : SearchBooksUseCase {
            override suspend fun execute(query: String) = MutableStateFlow(emptyList<EpubBooks>()).asStateFlow()
        }
    ) {}

    LibraryScreen(
        onBookClick = {},
        onImportBook = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyLibraryPreview() {
    EmptyLibrary()
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    ErrorState(
        message = "Something went wrong",
        onRetry = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchTopBarPreview() {
    SearchTopBar(
        searchQuery = "Sample search",
        onSearchQueryChange = {}
    )
}
