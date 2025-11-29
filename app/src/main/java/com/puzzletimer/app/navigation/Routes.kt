package com.puzzletimer.app.navigation

/**
 * Navigation routes for the Puzzle Timer app.
 * Uses a sealed class for type-safe navigation.
 */
sealed class Routes(val route: String) {
    /**
     * Home screen - displays the last puzzle and quick actions
     */
    data object Home : Routes("home")

    /**
     * New Puzzle screen - form to create a new puzzle
     */
    data object NewPuzzle : Routes("new_puzzle")

    /**
     * Timer screen - displays the timer for an active session
     * Requires sessionId parameter
     */
    data object Timer : Routes("timer")

    /**
     * Puzzle Details screen - displays details and history for a puzzle
     * Requires puzzleId parameter
     */
    data object PuzzleDetails : Routes("puzzle_details")

    /**
     * Search screen - search and filter puzzles
     */
    data object Search : Routes("search")
}
