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
     * Recalculates puzzle statistics and reloads the puzzle data to update the UI.
     * @param session The session to delete
     */
    fun deleteSession(session: PuzzleSession) {
        viewModelScope.launch {
            sessionRepository.deleteSession(session)

            // Recalculate puzzle stats based on remaining sessions
            _currentPuzzleId?.let { puzzleId ->
                val remainingSessions = sessionRepository.getSessionsForPuzzle(puzzleId).firstOrNull() ?: emptyList()
                puzzleRepository.recalculatePuzzleStats(puzzleId, remainingSessions)

                // Reload puzzle to update UI
                loadPuzzle(puzzleId)
            }
        }
    }

    /**
     * Restore a previously deleted session.
     * Used for undo functionality after swipe-to-delete.
     * Recalculates puzzle statistics and reloads the puzzle data.
     * @param session The session to restore
     */
    fun restoreSession(session: PuzzleSession) {
        viewModelScope.launch {
            sessionRepository.insertSession(session)

            // Recalculate puzzle stats including the restored session
            _currentPuzzleId?.let { puzzleId ->
                val allSessions = sessionRepository.getSessionsForPuzzle(puzzleId).firstOrNull() ?: emptyList()
                puzzleRepository.recalculatePuzzleStats(puzzleId, allSessions)

                // Reload puzzle to update UI
                loadPuzzle(puzzleId)
            }
        }
    }

    /**
     * Update puzzle details (name, piece count, image).
     * @param name The new puzzle name
     * @param pieceCount The new piece count
     * @param imageUri The new image URI (or null to keep existing)
     */
    suspend fun updatePuzzleDetails(
        name: String,
        pieceCount: Int,
        imageUri: String?
    ): Result<Unit> {
        val currentPuzzle = _puzzle.value ?: return Result.failure(Exception("No puzzle loaded"))

        // Validate inputs
        if (name.isBlank()) {
            return Result.failure(Exception("Puzzle name cannot be empty"))
        }

        if (pieceCount <= 0) {
            return Result.failure(Exception("Piece count must be greater than 0"))
        }

        try {
            val updatedPuzzle = currentPuzzle.copy(
                name = name.trim(),
                pieceCount = pieceCount,
                imageUri = imageUri ?: currentPuzzle.imageUri
            )

            puzzleRepository.updatePuzzle(updatedPuzzle)
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
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
