package com.puzzletimer.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzletimer.app.data.model.Puzzle
import com.puzzletimer.app.repository.PuzzleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Search screen.
 * Manages the state of completed puzzles for search and browsing.
 */
class SearchViewModel(
    private val puzzleRepository: PuzzleRepository
) : ViewModel() {

    private val _completedPuzzles = MutableStateFlow<List<Puzzle>>(emptyList())
    val completedPuzzles: StateFlow<List<Puzzle>> = _completedPuzzles.asStateFlow()

    init {
        loadCompletedPuzzles()
    }

    /**
     * Load all completed puzzles from the repository.
     * Puzzles are automatically ordered by lastCompletedDate DESC from the repository.
     */
    private fun loadCompletedPuzzles() {
        viewModelScope.launch {
            puzzleRepository.getAllPuzzles().collect { puzzles ->
                _completedPuzzles.value = puzzles
            }
        }
    }
}
