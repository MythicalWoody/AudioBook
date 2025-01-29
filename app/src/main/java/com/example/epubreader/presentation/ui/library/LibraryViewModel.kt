package com.example.epubreader.presentation.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.usecase.interfaces.GetRecentBooksUseCase
import com.example.epubreader.domain.usecase.interfaces.SearchBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LibraryUiState {
    object Loading : LibraryUiState()
    data class Success(val books: List<EpubBooks>) : LibraryUiState()
    data class Error(val message: String) : LibraryUiState()
}

@HiltViewModel
open class LibraryViewModel @Inject constructor(
    private val getRecentBooksUseCase: GetRecentBooksUseCase,
    private val searchBooksUseCase: SearchBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = LibraryUiState.Loading
            getRecentBooksUseCase.execute()
                .catch { e ->
                    _uiState.value = LibraryUiState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { books ->
                    _uiState.value = LibraryUiState.Success(books)
                }
        }
    }

    fun searchBooks(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadBooks()
            return
        }

        viewModelScope.launch {
            _uiState.value = LibraryUiState.Loading
            searchBooksUseCase.execute(query)
                .catch { e ->
                    _uiState.value = LibraryUiState.Error(e.message ?: "Search failed")
                }
                .collect { books ->
                    _uiState.value = LibraryUiState.Success(books)
                }
        }
    }
}
