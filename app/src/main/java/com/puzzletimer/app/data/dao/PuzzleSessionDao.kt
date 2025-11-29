package com.puzzletimer.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.puzzletimer.app.data.model.PuzzleSession
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for PuzzleSession entity.
 * Provides methods for querying, inserting, updating, and deleting puzzle sessions.
 */
@Dao
interface PuzzleSessionDao {

    /**
     * Get all sessions for a specific puzzle ordered by start time (most recent first).
     * @param puzzleId The ID of the puzzle to get sessions for
     * @return Flow of sessions for the specified puzzle
     */
    @Query("SELECT * FROM puzzle_sessions WHERE puzzleId = :puzzleId ORDER BY startTime DESC")
    fun getSessionsForPuzzle(puzzleId: Long): Flow<List<PuzzleSession>>

    /**
     * Get a single session by its ID.
     * @param id The session ID to retrieve
     * @return Flow of the session, or null if not found
     */
    @Query("SELECT * FROM puzzle_sessions WHERE id = :id")
    fun getSessionById(id: Long): Flow<PuzzleSession?>

    /**
     * Get the currently active session (session with null endTime).
     * There should only be one active session at a time.
     * @return Flow of the active session, or null if no session is active
     */
    @Query("SELECT * FROM puzzle_sessions WHERE endTime IS NULL LIMIT 1")
    fun getActiveSession(): Flow<PuzzleSession?>

    /**
     * Insert a new session into the database.
     * @param session The session to insert
     * @return The ID of the newly inserted session
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PuzzleSession): Long

    /**
     * Update an existing session in the database.
     * @param session The session with updated values
     */
    @Update
    suspend fun updateSession(session: PuzzleSession)

    /**
     * Delete a session from the database.
     * @param session The session to delete
     */
    @Delete
    suspend fun deleteSession(session: PuzzleSession)
}
