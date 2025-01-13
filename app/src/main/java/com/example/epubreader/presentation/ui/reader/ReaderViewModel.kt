package com.example.epubreader.presentation.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epubreader.domain.ReadBookUseCase
import com.example.epubreader.domain.UpdateReadingPositionUseCase
import com.example.epubreader.domain.model.EpubBooks
import com.example.epubreader.domain.model.ReadingPosition
import com.example.epubreader.presentation.ui.reader.components.TextToSpeechController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val readBookUseCase: ReadBookUseCase,
    private val updatePositionUseCase: UpdateReadingPositionUseCase,
    private val textToSpeechController: TextToSpeechController
) : ViewModel() {
    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            readBookUseCase.execute(bookId)
                .catch { error ->
                    _uiState.value = ReaderUiState.Error(error.message)
                }
                .collect { book ->
                    _uiState.value = ReaderUiState.Success(book)
                }
        }
    }

    fun updateReadingPosition(position: ReadingPosition) {
        viewModelScope.launch {
            (uiState.value as? ReaderUiState.Success)?.book?.id?.let { bookId ->
                updatePositionUseCase.execute(bookId, position)
            }
        }
    }
}

sealed class ReaderUiState {
    object Loading : ReaderUiState()
    data class Success(val book: EpubBooks) : ReaderUiState()
    data class Error(val message: String?) : ReaderUiState()
}