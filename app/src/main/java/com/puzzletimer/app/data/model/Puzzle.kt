package com.puzzletimer.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a puzzle in the database.
 * Tracks puzzle metadata, completion history, and statistics.
 */
@Entity(tableName = "puzzles")
data class Puzzle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Name of the puzzle (e.g., "Mountain Scenery")
     */
    val name: String,

    /**
     * Number of pieces in the puzzle (e.g., 1000)
     */
    val pieceCount: Int,

    /**
     * Path to the stored puzzle image (nullable if no image)
     */
    val imageUri: String? = null,

    /**
     * Brand or manufacturer of the puzzle (e.g., "Ravensburger", "Buffalo Games")
     * Nullable for backward compatibility with existing database entries
     */
    val brand: String? = null,

    /**
     * Timestamp when the puzzle was first completed (nullable if never completed)
     */
    val firstCompletedDate: Long? = null,

    /**
     * Timestamp when the puzzle was last completed (nullable if never completed)
     */
    val lastCompletedDate: Long? = null,

    /**
     * Total number of times this puzzle has been completed
     */
    val totalCompletions: Int = 0,

    /**
     * Best time in milliseconds for completing this puzzle (nullable if never completed)
     */
    val bestTimeMillis: Long? = null,

    /**
     * Average time in milliseconds for completing this puzzle (nullable if never completed)
     */
    val averageTimeMillis: Long? = null
)
