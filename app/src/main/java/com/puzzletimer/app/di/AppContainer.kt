package com.puzzletimer.app.di

import android.content.Context
import com.puzzletimer.app.data.dao.PuzzleDao
import com.puzzletimer.app.data.dao.PuzzleSessionDao
import com.puzzletimer.app.data.database.AppDatabase
import com.puzzletimer.app.repository.PuzzleRepository
import com.puzzletimer.app.repository.SessionRepository

/**
 * Manual dependency injection container for the application.
 * Provides singleton instances of database, DAOs, and repositories.
 */
class AppContainer(private val context: Context) {

    /**
     * Lazily initialized Room database instance.
     * Uses singleton pattern via AppDatabase.getDatabase().
     */
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    /**
     * Lazily initialized PuzzleDao for puzzle data operations.
     */
    val puzzleDao: PuzzleDao by lazy {
        database.puzzleDao()
    }

    /**
     * Lazily initialized PuzzleSessionDao for session data operations.
     */
    val puzzleSessionDao: PuzzleSessionDao by lazy {
        database.puzzleSessionDao()
    }

    /**
     * Lazily initialized PuzzleRepository for puzzle business logic.
     */
    val puzzleRepository: PuzzleRepository by lazy {
        PuzzleRepository(puzzleDao)
    }

    /**
     * Lazily initialized SessionRepository for session business logic.
     */
    val sessionRepository: SessionRepository by lazy {
        SessionRepository(puzzleSessionDao)
    }
}
