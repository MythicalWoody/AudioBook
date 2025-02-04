package com.example.epubreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.epubreader.domain.repository.BookRepository
import com.example.epubreader.presentation.ui.library.LibraryScreen
import com.example.epubreader.ui.theme.EpubReaderTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bookRepository: BookRepository

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedUri ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    bookRepository.importBook(selectedUri, applicationContext)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EpubReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LibraryScreen(
                        onBookClick = { bookId -> /* Navigate to ReaderScreen */ },
                        onImportBook = { getContent.launch("application/epub+zip") },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}