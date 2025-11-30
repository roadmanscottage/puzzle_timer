package com.puzzletimer.app.repository

import com.puzzletimer.app.data.dao.PuzzleDao
import com.puzzletimer.app.data.model.Puzzle
import com.puzzletimer.app.data.model.PuzzleSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Repository for managing Puzzle data operations.
 * Acts as the single source of truth for puzzle-related data.
 */
class PuzzleRepository(private val puzzleDao: PuzzleDao) {

    /**
     * Get all puzzles as a Flow.
     * Puzzles are ordered by last completed date (most recent first).
     */
    fun getAllPuzzles(): Flow<List<Puzzle>> {
        return puzzleDao.getAllPuzzles()
    }

    /**
     * Get a single puzzle by ID as a Flow.
     * @param id The puzzle ID to retrieve
     * @return Flow of the puzzle, or null if not found
     */
    fun getPuzzleById(id: Long): Flow<Puzzle?> {
        return puzzleDao.getPuzzleById(id)
    }

    /**
     * Get the most recently completed puzzle as a Flow.
     * @return Flow of the last completed puzzle, or null if no puzzles completed
     */
    fun getLastCompletedPuzzle(): Flow<Puzzle?> {
        return puzzleDao.getLastCompletedPuzzle()
    }

    /**
     * Search for puzzles by name (case-insensitive).
     * @param query The search query to match against puzzle names
     * @return Flow of puzzles matching the search query
     */
    fun searchPuzzles(query: String): Flow<List<Puzzle>> {
        return puzzleDao.searchPuzzles(query)
    }

    /**
     * Insert a new puzzle into the database.
     * @param puzzle The puzzle to insert
     * @return The ID of the newly inserted puzzle
     */
    suspend fun insertPuzzle(puzzle: Puzzle): Long {
        return puzzleDao.insertPuzzle(puzzle)
    }

    /**
     * Update an existing puzzle in the database.
     * @param puzzle The puzzle with updated values
     */
    suspend fun updatePuzzle(puzzle: Puzzle) {
        puzzleDao.updatePuzzle(puzzle)
    }

    /**
     * Delete a puzzle from the database.
     * This will also delete all associated sessions due to CASCADE foreign key.
     * @param puzzle The puzzle to delete
     */
    suspend fun deletePuzzle(puzzle: Puzzle) {
        puzzleDao.deletePuzzle(puzzle)
    }

    /**
     * Update puzzle statistics after a session is completed.
     * Updates completion dates, total completions, best time, and average time.
     * @param puzzleId The ID of the puzzle to update
     * @param sessionTime The elapsed time of the completed session in milliseconds
     */
    suspend fun updatePuzzleStats(puzzleId: Long, sessionTime: Long) {
        val puzzleFlow = puzzleDao.getPuzzleById(puzzleId)
        val puzzle = puzzleFlow.firstOrNull() ?: return

        val currentTime = System.currentTimeMillis()
        val newTotalCompletions = puzzle.totalCompletions + 1

        // Calculate new best time
        val newBestTime = if (puzzle.bestTimeMillis == null) {
            sessionTime
        } else {
            minOf(puzzle.bestTimeMillis, sessionTime)
        }

        // Calculate new average time
        val newAverageTime = if (puzzle.averageTimeMillis == null) {
            sessionTime
        } else {
            ((puzzle.averageTimeMillis * puzzle.totalCompletions) + sessionTime) / newTotalCompletions
        }

        val updatedPuzzle = puzzle.copy(
            firstCompletedDate = puzzle.firstCompletedDate ?: currentTime,
            lastCompletedDate = currentTime,
            totalCompletions = newTotalCompletions,
            bestTimeMillis = newBestTime,
            averageTimeMillis = newAverageTime
        )

        puzzleDao.updatePuzzle(updatedPuzzle)
    }

    /**
     * Recalculate puzzle statistics based on all completed sessions.
     * This is called after a session is deleted or restored to ensure statistics are accurate.
     * @param puzzleId The ID of the puzzle to recalculate
     * @param completedSessions List of all completed sessions for this puzzle
     */
    suspend fun recalculatePuzzleStats(puzzleId: Long, completedSessions: List<PuzzleSession>) {
        val puzzleFlow = puzzleDao.getPuzzleById(puzzleId)
        val puzzle = puzzleFlow.firstOrNull() ?: return

        // Filter to only completed sessions with valid elapsed time
        val validSessions = completedSessions.filter { it.isCompleted && it.elapsedTimeMillis > 0 }

        if (validSessions.isEmpty()) {
            // No completed sessions - reset all stats
            val updatedPuzzle = puzzle.copy(
                firstCompletedDate = null,
                lastCompletedDate = null,
                totalCompletions = 0,
                bestTimeMillis = null,
                averageTimeMillis = null
            )
            puzzleDao.updatePuzzle(updatedPuzzle)
        } else {
            // Calculate stats from remaining sessions
            val bestTime = validSessions.minOf { it.elapsedTimeMillis }
            val averageTime = validSessions.map { it.elapsedTimeMillis }.average().toLong()
            val totalCompletions = validSessions.size

            // Find first and last completion dates
            val firstCompleted = validSessions.minOfOrNull { it.endTime ?: it.startTime }
            val lastCompleted = validSessions.maxOfOrNull { it.endTime ?: it.startTime }

            val updatedPuzzle = puzzle.copy(
                firstCompletedDate = firstCompleted,
                lastCompletedDate = lastCompleted,
                totalCompletions = totalCompletions,
                bestTimeMillis = bestTime,
                averageTimeMillis = averageTime
            )
            puzzleDao.updatePuzzle(updatedPuzzle)
        }
    }
}
