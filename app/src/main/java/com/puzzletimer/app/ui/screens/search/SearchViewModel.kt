package com.puzzletimer.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzletimer.app.data.model.Puzzle
import com.puzzletimer.app.data.preferences.UserPreferencesManager
import com.puzzletimer.app.repository.PuzzleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Search screen.
 * Manages the state of completed puzzles for search and browsing.
 */
class SearchViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _completedPuzzles = MutableStateFlow<List<Puzzle>>(emptyList())
    val completedPuzzles: StateFlow<List<Puzzle>> = _completedPuzzles.asStateFlow()

    /**
     * Flow of the saved sort preference.
     * Emits "NAME" or "DATE_COMPLETED"
     */
    val sortPreference: StateFlow<String> = preferencesManager.sortPreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferencesManager.DEFAULT_SORT_OPTION
        )

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

    /**
     * Save the user's sort preference.
     * @param sortOption The sort option string ("NAME" or "DATE_COMPLETED")
     */
    fun saveSortPreference(sortOption: String) {
        viewModelScope.launch {
            preferencesManager.saveSortPreference(sortOption)
        }
    }

    /**
     * Delete a puzzle from the database.
     * This will also delete all associated sessions due to CASCADE foreign key.
     * @param puzzle The puzzle to delete
     */
    fun deletePuzzle(puzzle: Puzzle) {
        viewModelScope.launch {
            puzzleRepository.deletePuzzle(puzzle)
        }
    }

    /**
     * Restore a previously deleted puzzle to the database.
     * Used for undo functionality after deletion.
     * @param puzzle The puzzle to restore
     */
    fun restorePuzzle(puzzle: Puzzle) {
        viewModelScope.launch {
            puzzleRepository.insertPuzzle(puzzle)
        }
    }
}
