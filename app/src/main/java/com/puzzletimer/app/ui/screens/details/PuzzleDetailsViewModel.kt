package com.puzzletimer.app.ui.screens.details

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
 * ViewModel for the Puzzle Details screen.
 * Manages puzzle details, session history, and puzzle operations.
 */
class PuzzleDetailsViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _puzzle = MutableStateFlow<Puzzle?>(null)
    val puzzle: StateFlow<Puzzle?> = _puzzle.asStateFlow()

    private val _sessions = MutableStateFlow<List<PuzzleSession>>(emptyList())
    val sessions: StateFlow<List<PuzzleSession>> = _sessions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var _currentPuzzleId: Long? = null

    /**
     * Load a puzzle by ID and its associated sessions.
     * @param puzzleId The ID of the puzzle to load
     */
    fun loadPuzzle(puzzleId: Long) {
        _currentPuzzleId = puzzleId

        viewModelScope.launch {
            _isLoading.value = true

            // Load the puzzle
            puzzleRepository.getPuzzleById(puzzleId).collect { loadedPuzzle ->
                _puzzle.value = loadedPuzzle
            }
        }

        viewModelScope.launch {
            // Load the sessions for this puzzle
            sessionRepository.getSessionsForPuzzle(puzzleId).collect { sessionList ->
                _sessions.value = sessionList
                _isLoading.value = false
            }
        }
    }

    /**
     * Start a new session for the current puzzle.
     * @return The ID of the newly created session, or null if no puzzle is loaded
     */
    suspend fun startNewSession(): Long? {
        val puzzleId = _currentPuzzleId ?: return null

        val session = PuzzleSession(
            puzzleId = puzzleId,
            startTime = System.currentTimeMillis()
        )

        return sessionRepository.insertSession(session)
    }

    /**
     * Delete the current puzzle from the database.
     * This will also delete all associated sessions due to CASCADE.
     */
    suspend fun deletePuzzle() {
        val puzzle = _puzzle.value ?: return
        puzzleRepository.deletePuzzle(puzzle)
    }

    /**
     * Delete a session from the database.
     * Reloads the puzzle data to update the UI.
     * @param session The session to delete
     */
    fun deleteSession(session: PuzzleSession) {
        viewModelScope.launch {
            sessionRepository.deleteSession(session)
            // Reload puzzle to update UI
            _currentPuzzleId?.let { loadPuzzle(it) }
        }
    }

    /**
     * Share the puzzle details.
     * Placeholder for future sharing feature implementation.
     */
    fun sharePuzzle() {
        // Placeholder for future sharing functionality
        // Could generate a shareable summary of puzzle stats
        // and open system share dialog
    }
}
