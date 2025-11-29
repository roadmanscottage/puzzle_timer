package com.puzzletimer.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.puzzletimer.app.data.model.Puzzle
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Puzzle entity.
 * Provides methods for querying, inserting, updating, and deleting puzzles.
 */
@Dao
interface PuzzleDao {

    /**
     * Get all puzzles ordered by last completed date (most recent first).
     * Puzzles that have never been completed will appear at the end.
     */
    @Query("SELECT * FROM puzzles ORDER BY lastCompletedDate DESC")
    fun getAllPuzzles(): Flow<List<Puzzle>>

    /**
     * Get a single puzzle by its ID.
     * @param id The puzzle ID to retrieve
     * @return Flow of the puzzle, or null if not found
     */
    @Query("SELECT * FROM puzzles WHERE id = :id")
    fun getPuzzleById(id: Long): Flow<Puzzle?>

    /**
     * Get the most recently completed puzzle.
     * @return Flow of the puzzle with the most recent lastCompletedDate, or null if no puzzles completed
     */
    @Query("SELECT * FROM puzzles WHERE lastCompletedDate IS NOT NULL ORDER BY lastCompletedDate DESC LIMIT 1")
    fun getLastCompletedPuzzle(): Flow<Puzzle?>

    /**
     * Search for puzzles by name (case-insensitive).
     * @param query The search query to match against puzzle names
     * @return Flow of puzzles matching the search query
     */
    @Query("SELECT * FROM puzzles WHERE name LIKE '%' || :query || '%' ORDER BY lastCompletedDate DESC")
    fun searchPuzzles(query: String): Flow<List<Puzzle>>

    /**
     * Insert a new puzzle into the database.
     * @param puzzle The puzzle to insert
     * @return The ID of the newly inserted puzzle
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzle(puzzle: Puzzle): Long

    /**
     * Update an existing puzzle in the database.
     * @param puzzle The puzzle with updated values
     */
    @Update
    suspend fun updatePuzzle(puzzle: Puzzle)

    /**
     * Delete a puzzle from the database.
     * This will also delete all associated sessions due to CASCADE foreign key.
     * @param puzzle The puzzle to delete
     */
    @Delete
    suspend fun deletePuzzle(puzzle: Puzzle)
}
