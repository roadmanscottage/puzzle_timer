package com.puzzletimer.app.repository

import com.puzzletimer.app.data.dao.PuzzleSessionDao
import com.puzzletimer.app.data.model.PuzzleSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Repository for managing PuzzleSession data operations.
 * Acts as the single source of truth for session-related data.
 */
class SessionRepository(private val sessionDao: PuzzleSessionDao) {

    /**
     * Get all sessions for a specific puzzle as a Flow.
     * Sessions are ordered by start time (most recent first).
     * @param puzzleId The ID of the puzzle to get sessions for
     * @return Flow of sessions for the specified puzzle
     */
    fun getSessionsForPuzzle(puzzleId: Long): Flow<List<PuzzleSession>> {
        return sessionDao.getSessionsForPuzzle(puzzleId)
    }

    /**
     * Get a single session by ID as a Flow.
     * @param id The session ID to retrieve
     * @return Flow of the session, or null if not found
     */
    fun getSessionById(id: Long): Flow<PuzzleSession?> {
        return sessionDao.getSessionById(id)
    }

    /**
     * Get the currently active session (session with null endTime) as a Flow.
     * @return Flow of the active session, or null if no session is active
     */
    fun getActiveSession(): Flow<PuzzleSession?> {
        return sessionDao.getActiveSession()
    }

    /**
     * Insert a new session into the database.
     * @param session The session to insert
     * @return The ID of the newly inserted session
     */
    suspend fun insertSession(session: PuzzleSession): Long {
        return sessionDao.insertSession(session)
    }

    /**
     * Update an existing session in the database.
     * Used for pause/resume functionality.
     * @param session The session with updated values
     */
    suspend fun updateSession(session: PuzzleSession) {
        sessionDao.updateSession(session)
    }

    /**
     * Delete a session from the database.
     * @param session The session to delete
     */
    suspend fun deleteSession(session: PuzzleSession) {
        sessionDao.deleteSession(session)
    }

    /**
     * Complete a session by setting end time, elapsed time, and marking as completed.
     * @param sessionId The ID of the session to complete
     * @param endTime The timestamp when the session ended
     * @param elapsedTime The total elapsed time in milliseconds
     */
    suspend fun completeSession(sessionId: Long, endTime: Long, elapsedTime: Long) {
        val sessionFlow = sessionDao.getSessionById(sessionId)
        val session = sessionFlow.firstOrNull() ?: return

        val completedSession = session.copy(
            endTime = endTime,
            elapsedTimeMillis = elapsedTime,
            isCompleted = true,
            pausedAt = null // Clear paused state
        )

        sessionDao.updateSession(completedSession)
    }
}
