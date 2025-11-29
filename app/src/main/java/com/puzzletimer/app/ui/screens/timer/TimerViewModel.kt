package com.puzzletimer.app.ui.screens.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzletimer.app.repository.PuzzleRepository
import com.puzzletimer.app.repository.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * ViewModel for the Timer screen.
 * Manages timer state, puzzle session tracking, and timer operations.
 */
class TimerViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _puzzleName = MutableStateFlow("")
    val puzzleName: StateFlow<String> = _puzzleName.asStateFlow()

    private val _pieceCount = MutableStateFlow(0)
    val pieceCount: StateFlow<Int> = _pieceCount.asStateFlow()

    private val _elapsedTimeMillis = MutableStateFlow(0L)
    val elapsedTimeMillis: StateFlow<Long> = _elapsedTimeMillis.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private var timerJob: Job? = null
    private var currentSessionId: Long? = null
    private var pausedElapsedTime: Long = 0L

    /**
     * Load a session by ID and initialize the timer state.
     * @param sessionId The ID of the session to load
     */
    fun loadSession(sessionId: Long) {
        currentSessionId = sessionId

        viewModelScope.launch {
            // Load the session
            val session = sessionRepository.getSessionById(sessionId).firstOrNull()
            if (session == null) return@launch

            // Load the puzzle
            val puzzle = puzzleRepository.getPuzzleById(session.puzzleId).firstOrNull()
            if (puzzle == null) return@launch

            _puzzleName.value = puzzle.name
            _pieceCount.value = puzzle.pieceCount

            // Initialize elapsed time from session
            pausedElapsedTime = session.elapsedTimeMillis
            _elapsedTimeMillis.value = pausedElapsedTime

            // Check if session was paused
            if (session.pausedAt != null) {
                _isPaused.value = true
                _isRunning.value = false
            } else if (session.endTime == null) {
                // Session is active, start the timer
                startTimerJob()
            }
        }
    }

    /**
     * Start the timer job that increments elapsed time every 10ms.
     */
    private fun startTimerJob() {
        _isRunning.value = true
        _isPaused.value = false

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (true) {
                delay(10L)
                val currentTime = System.currentTimeMillis()
                _elapsedTimeMillis.value = pausedElapsedTime + (currentTime - startTime)
            }
        }
    }

    /**
     * Pause the timer and update the session in the database.
     */
    suspend fun pauseTimer() {
        if (!_isRunning.value) return

        _isRunning.value = false
        _isPaused.value = true

        timerJob?.cancel()
        timerJob = null

        // Store the current elapsed time
        pausedElapsedTime = _elapsedTimeMillis.value

        // Update the session with paused state
        val sessionId = currentSessionId ?: return
        val session = sessionRepository.getSessionById(sessionId).firstOrNull() ?: return

        val pausedSession = session.copy(
            elapsedTimeMillis = pausedElapsedTime,
            pausedAt = System.currentTimeMillis()
        )

        sessionRepository.updateSession(pausedSession)
    }

    /**
     * Resume the timer from paused state.
     */
    suspend fun resumeTimer() {
        if (_isRunning.value || !_isPaused.value) return

        // Update the session to clear paused state
        val sessionId = currentSessionId ?: return
        val session = sessionRepository.getSessionById(sessionId).firstOrNull() ?: return

        val resumedSession = session.copy(
            pausedAt = null
        )

        sessionRepository.updateSession(resumedSession)

        // Start the timer job
        startTimerJob()
    }

    /**
     * Finish the puzzle by completing the session and updating puzzle statistics.
     * @return The puzzle ID if successful, null otherwise
     */
    suspend fun finishPuzzle(): Long? {
        _isRunning.value = false
        _isPaused.value = false

        timerJob?.cancel()
        timerJob = null

        val sessionId = currentSessionId ?: return null
        val finalElapsedTime = _elapsedTimeMillis.value
        val endTime = System.currentTimeMillis()

        // Complete the session
        sessionRepository.completeSession(sessionId, endTime, finalElapsedTime)

        // Get the session to find puzzle ID
        val session = sessionRepository.getSessionById(sessionId).firstOrNull() ?: return null

        // Update puzzle statistics
        puzzleRepository.updatePuzzleStats(session.puzzleId, finalElapsedTime)

        return session.puzzleId
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
