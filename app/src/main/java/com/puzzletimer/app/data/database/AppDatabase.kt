package com.puzzletimer.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.puzzletimer.app.data.dao.PuzzleDao
import com.puzzletimer.app.data.dao.PuzzleSessionDao
import com.puzzletimer.app.data.model.Puzzle
import com.puzzletimer.app.data.model.PuzzleSession

/**
 * Room database for the Puzzle Timer app.
 * Contains entities for puzzles and puzzle sessions.
 */
@Database(
    entities = [Puzzle::class, PuzzleSession::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to Puzzle data operations.
     */
    abstract fun puzzleDao(): PuzzleDao

    /**
     * Provides access to PuzzleSession data operations.
     */
    abstract fun puzzleSessionDao(): PuzzleSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from database version 1 to 2.
         * Adds brand column to puzzles table for storing puzzle manufacturer/brand.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE puzzles ADD COLUMN brand TEXT")
            }
        }

        /**
         * Get the singleton database instance.
         * Uses double-checked locking pattern to ensure thread safety.
         * @param context Application context
         * @return The database instance
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "puzzle_timer_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
