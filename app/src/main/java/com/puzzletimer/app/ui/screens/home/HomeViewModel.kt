package com.puzzletimer.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzletimer.app.data.model.Puzzle
import com.puzzletimer.app.data.model.PuzzleSession
import com.puzzletimer.app.repository.PuzzleRepository
import com.puzzletimer.app.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Data class to hold paused session information for display
 */
data class PausedSessionInfo(
    val session: PuzzleSession,
    val puzzle: Puzzle
)

/**
 * ViewModel for the Home screen.
 * Manages the state of the last completed puzzle, paused sessions, and loading indicators.
 */
class HomeViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _lastCompletedPuzzle = MutableStateFlow<Puzzle?>(null)
    val lastCompletedPuzzle: StateFlow<Puzzle?> = _lastCompletedPuzzle.asStateFlow()

    private val _pausedSessionInfo = MutableStateFlow<PausedSessionInfo?>(null)
    val pausedSessionInfo: StateFlow<PausedSessionInfo?> = _pausedSessionInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHomeScreenData()
    }

    /**
     * Load home screen data: check for paused sessions first, then load last completed puzzle.
     * Paused sessions take priority and will be displayed prominently.
     */
    private fun loadHomeScreenData() {
        viewModelScope.launch {
            _isLoading.value = true

            // First, check for paused sessions
            val pausedSession = sessionRepository.getMostRecentPausedSession().firstOrNull()

            if (pausedSession != null) {
                // Load the puzzle for this paused session
                val puzzle = puzzleRepository.getPuzzleById(pausedSession.puzzleId).firstOrNull()
                if (puzzle != null) {
                    _pausedSessionInfo.value = PausedSessionInfo(pausedSession, puzzle)
                }
            }

            // Also load the last completed puzzle for display
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
