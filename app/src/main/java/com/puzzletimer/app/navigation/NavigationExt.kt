package com.puzzletimer.app.navigation

import androidx.navigation.NavController

/**
 * Extension functions for NavController to provide type-safe navigation.
 */

/**
 * Navigate to the Timer screen with a sessionId
 */
fun NavController.navigateToTimer(sessionId: Long) {
    navigate("${Routes.Timer.route}/$sessionId")
}

/**
 * Navigate to the Puzzle Details screen with a puzzleId
 */
fun NavController.navigateToPuzzleDetails(puzzleId: Long) {
    navigate("${Routes.PuzzleDetails.route}/$puzzleId")
}

/**
 * Navigate to the New Puzzle screen
 */
fun NavController.navigateToNewPuzzle() {
    navigate(Routes.NewPuzzle.route)
}

/**
 * Navigate to the Search screen
 */
fun NavController.navigateToSearch() {
    navigate(Routes.Search.route)
}

/**
 * Navigate to the Home screen
 */
fun NavController.navigateToHome() {
    navigate(Routes.Home.route)
}
