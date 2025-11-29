package com.puzzletimer.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzletimer.app.data.model.Puzzle
import com.puzzletimer.app.repository.PuzzleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen.
 * Manages the state of the last completed puzzle and loading indicators.
 */
class HomeViewModel(
    private val puzzleRepository: PuzzleRepository
) : ViewModel() {

    private val _lastCompletedPuzzle = MutableStateFlow<Puzzle?>(null)
    val lastCompletedPuzzle: StateFlow<Puzzle?> = _lastCompletedPuzzle.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLastCompletedPuzzle()
    }

    /**
     * Load the most recently completed puzzle from the repository.
     */
    private fun loadLastCompletedPuzzle() {
        viewModelScope.launch {
            _isLoading.value = true
            puzzleRepository.getLastCompletedPuzzle().collect { puzzle ->
                _lastCompletedPuzzle.value = puzzle
                _isLoading.value = false
            }
        }
    }

    /**
     * Navigate to the new puzzle creation screen.
     * This method can be extended with navigation logic when NavController is integrated.
     */
    fun navigateToNewPuzzle() {
        // Navigation logic will be handled by the composable screen
    }

    /**
     * Navigate to the puzzle search screen.
     * This method can be extended with navigation logic when NavController is integrated.
     */
    fun navigateToSearch() {
        // Navigation logic will be handled by the composable screen
    }

    /**
     * Navigate to the puzzle details screen for a specific puzzle.
     * @param puzzleId The ID of the puzzle to view details for
     */
    fun navigateToPuzzleDetails(puzzleId: Long) {
        // Navigation logic will be handled by the composable screen
    }
}
