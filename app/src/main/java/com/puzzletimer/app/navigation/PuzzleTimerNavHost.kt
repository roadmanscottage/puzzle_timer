package com.puzzletimer.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.puzzletimer.app.ui.screens.details.PuzzleDetailsScreen
import com.puzzletimer.app.ui.screens.home.HomeScreen
import com.puzzletimer.app.ui.screens.newpuzzle.NewPuzzleScreen
import com.puzzletimer.app.ui.screens.search.SearchScreen
import com.puzzletimer.app.ui.screens.timer.TimerScreen

/**
 * Navigation host for the Puzzle Timer app.
 * Defines all navigation routes and screen compositions.
 */
@Composable
fun PuzzleTimerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ) {
        // Home Screen - Main landing screen with last puzzle
        composable(route = Routes.Home.route) {
            HomeScreen(
                onNavigateToNewPuzzle = {
                    navController.navigateToNewPuzzle()
                },
                onNavigateToSearch = {
                    navController.navigateToSearch()
                },
                onNavigateToPuzzleDetails = { puzzleId ->
                    navController.navigateToPuzzleDetails(puzzleId)
                },
                onNavigateToTimer = { sessionId ->
                    navController.navigateToTimer(sessionId)
                }
            )
        }

        // New Puzzle Screen - Form to create a new puzzle
        composable(route = Routes.NewPuzzle.route) {
            NewPuzzleScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) { inclusive = false }
                    }
                },
                onNavigateToPuzzleDetails = { puzzleId ->
                    // Navigate to puzzle details after saving
                    // Remove NewPuzzle from back stack so back button goes to home
                    navController.navigate("${Routes.PuzzleDetails.route}/$puzzleId") {
                        popUpTo(Routes.NewPuzzle.route) { inclusive = true }
                    }
                },
                onNavigateToSearch = {
                    navController.navigateToSearch()
                }
            )
        }

        // Timer Screen - Displays timer for active session
        composable(
            route = "${Routes.Timer.route}/{sessionId}",
            arguments = listOf(
                navArgument("sessionId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            TimerScreen(
                sessionId = sessionId,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) { inclusive = false }
                    }
                },
                onNavigateToPuzzleDetails = { puzzleId ->
                    // When navigating from timer to puzzle details (after finishing),
                    // remove timer screen from back stack so back button goes to home
                    navController.navigate("${Routes.PuzzleDetails.route}/$puzzleId") {
                        popUpTo("${Routes.Timer.route}/{sessionId}") { inclusive = true }
                    }
                }
            )
        }

        // Puzzle Details Screen - Shows puzzle details and history
        composable(
            route = "${Routes.PuzzleDetails.route}/{puzzleId}",
            arguments = listOf(
                navArgument("puzzleId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val puzzleId = backStackEntry.arguments?.getLong("puzzleId") ?: 0L
            PuzzleDetailsScreen(
                puzzleId = puzzleId,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) { inclusive = false }
                    }
                },
                onNavigateToTimer = { sessionId ->
                    // Remove PuzzleDetails from back stack when starting/resuming timer
                    // so back button from timer goes to home
                    navController.navigate("${Routes.Timer.route}/$sessionId") {
                        popUpTo("${Routes.PuzzleDetails.route}/{puzzleId}") { inclusive = true }
                    }
                }
            )
        }

        // Search Screen - Search and filter puzzles
        composable(route = Routes.Search.route) {
            SearchScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) { inclusive = false }
                    }
                },
                onNavigateToPuzzleDetails = { puzzleId ->
                    navController.navigateToPuzzleDetails(puzzleId)
                }
            )
        }
    }
}
