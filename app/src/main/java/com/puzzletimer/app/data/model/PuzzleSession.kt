package com.puzzletimer.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a puzzle timing session in the database.
 * Tracks individual work sessions for each puzzle with timing information.
 */
@Entity(
    tableName = "puzzle_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Puzzle::class,
            parentColumns = ["id"],
            childColumns = ["puzzleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["puzzleId"])]
)
data class PuzzleSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Foreign key reference to the Puzzle this session belongs to
     */
    val puzzleId: Long,

    /**
     * Timestamp when this session started
     */
    val startTime: Long,

    /**
     * Timestamp when this session ended (nullable for ongoing sessions)
     */
    val endTime: Long? = null,

    /**
     * Total elapsed time in milliseconds for this session
     */
    val elapsedTimeMillis: Long = 0,

    /**
     * True if the session was completed (puzzle finished), false if abandoned
     */
    val isCompleted: Boolean = false,

    /**
     * Timestamp when this session was paused (nullable if not paused)
     */
    val pausedAt: Long? = null
)
