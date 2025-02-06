package com.example.epubreader.presentation.ui.library

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.epubreader.R
import com.example.epubreader.data.model.ReadingPreferences
import com.example.epubreader.data.model.TextAlignment
import com.example.epubreader.data.model.Theme
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.model.ImportBookParams
import com.example.epubreader.domain.model.ReadingPosition
import com.example.epubreader.domain.usecase.interfaces.DeleteBookUseCase
import com.example.epubreader.domain.usecase.interfaces.GetAllBooksUseCase
import com.example.epubreader.domain.usecase.interfaces.ImportBookUseCase
import com.example.epubreader.domain.usecase.interfaces.SearchBooksUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<EpubBooks?>(null) }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Import the selected EPUB file
                viewModel.importEpubBook(uri, context)
            }
        }
    }

    fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/epub+zip"
        }
        pickFileLauncher.launch(intent)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Book") },
            text = { Text("Are you sure you want to delete ${bookToDelete?.title}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        bookToDelete?.let { book ->
                            viewModel.deleteBook(book.id)
                        }
                        showDeleteDialog = false
                        bookToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        bookToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::searchBooks
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { launchFilePicker() }) {
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
                                onDeleteClick = {
                                    bookToDelete = book
                                    showDeleteDialog = true
                                },
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
    Log.d("Error state displayed:",message)
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
        object : GetAllBooksUseCase {
            override suspend fun execute(): Flow<List<EpubBooks>> = flow {
                emit(listOf(
                    EpubBooks(
                        id = "1",
                        title = "Sample Book 1",
                        chapters = listOf(),
                        currentPosition = ReadingPosition(0, 0),
                        readingPreferences = ReadingPreferences(
                            fontSize = 16f,
                            lineSpacing = 1.5f,
                            fontFamily = "Default",
                            theme = Theme.LIGHT,
                            textAlignment = TextAlignment.JUSTIFY
                        ),
                        dateAdded = Date(),
                        lastReadDate = Date()
                    ),
                    EpubBooks(
                        id = "2",
                        title = "Sample Book 2",
                        chapters = listOf(),
                        currentPosition = ReadingPosition(1, 50),
                        readingPreferences = ReadingPreferences(
                            fontSize = 16f,
                            lineSpacing = 1.5f,
                            fontFamily = "Default",
                            theme = Theme.LIGHT,
                            textAlignment = TextAlignment.JUSTIFY
                        ),
                        dateAdded = Date(),
                        lastReadDate = Date()
                    )
                ))
            }
        },
        object : SearchBooksUseCase {
            override suspend fun execute(query: String): Flow<List<EpubBooks>> = flow { emit(emptyList()) }
        },
        object : ImportBookUseCase {
            override suspend fun execute(params: ImportBookParams): String = ""
        },
        object : DeleteBookUseCase {
            override suspend fun execute(bookId: String) {}
        }
    ) {}

    LibraryScreen(
        onBookClick = {},
        onImportBook = {},
        viewModel = previewViewModel
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

@Composable
private fun BookCard(
    book: EpubBooks,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    if (book.coverImage != null) {
                        val bitmap = remember(book.coverImage) {
                            BitmapFactory.decodeByteArray(book.coverImage, 0, book.coverImage.size)
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Book cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillHeight
                            )
                        } else {
                            DefaultCoverImage()
                        }
                    } else {
                        DefaultCoverImage()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete book"
                )
            }
        }
    }
}

@Composable
private fun DefaultCoverImage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.book_material_icon),
            contentDescription = "Default book cover",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}
