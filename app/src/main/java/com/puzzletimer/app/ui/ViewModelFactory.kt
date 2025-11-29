package com.puzzletimer.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.puzzletimer.app.PuzzleTimerApplication
import com.puzzletimer.app.repository.PuzzleRepository
import com.puzzletimer.app.repository.SessionRepository
import com.puzzletimer.app.ui.screens.details.PuzzleDetailsViewModel
import com.puzzletimer.app.ui.screens.home.HomeViewModel
import com.puzzletimer.app.ui.screens.newpuzzle.NewPuzzleViewModel
import com.puzzletimer.app.ui.screens.timer.TimerViewModel

/**
 * Factory for creating ViewModels with repository dependencies.
 * Handles ViewModel instantiation with proper dependency injection.
 */
class ViewModelFactory(
    private val puzzleRepository: PuzzleRepository,
    private val sessionRepository: SessionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(puzzleRepository) as T
            }
            modelClass.isAssignableFrom(NewPuzzleViewModel::class.java) -> {
                NewPuzzleViewModel(puzzleRepository, sessionRepository) as T
            }
            modelClass.isAssignableFrom(TimerViewModel::class.java) -> {
                TimerViewModel(puzzleRepository, sessionRepository) as T
            }
            modelClass.isAssignableFrom(PuzzleDetailsViewModel::class.java) -> {
                PuzzleDetailsViewModel(puzzleRepository, sessionRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        /**
         * Create a ViewModelFactory instance with repositories from the AppContainer.
         * @param context The context to retrieve the application and container from
         * @return A configured ViewModelFactory instance
         */
        fun create(context: Context): ViewModelFactory {
            val application = context.applicationContext as PuzzleTimerApplication
            val container = application.container
            return ViewModelFactory(
                puzzleRepository = container.puzzleRepository,
                sessionRepository = container.sessionRepository
            )
        }
    }
}
