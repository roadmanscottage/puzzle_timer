package com.puzzletimer.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.puzzletimer.app.data.model.Puzzle
import com.puzzletimer.app.ui.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home screen - displays the last puzzle and quick actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel? = null,
    onNavigateToNewPuzzle: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPuzzleDetails: (Long) -> Unit,
    onNavigateToTimer: (Long) -> Unit
) {
    val actualViewModel = homeViewModel ?: viewModel(
        factory = ViewModelFactory.create(LocalContext.current)
    )
    val lastPuzzle by actualViewModel.lastCompletedPuzzle.collectAsState()
    val pausedSessionInfo by actualViewModel.pausedSessionInfo.collectAsState()
    val isLoading by actualViewModel.isLoading.collectAsState()

    // Reload data when home screen becomes visible
    LaunchedEffect(Unit) {
        actualViewModel.loadHomeScreenData()
    }

    Scaffold(
        topBar = { HomeTopAppBar() },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display paused puzzle OR last completed puzzle (paused takes priority)
            if (!isLoading) {
                if (pausedSessionInfo != null) {
                    // Paused Session Prompt (Priority Display)
                    PausedSessionPrompt(
                        pausedSessionInfo = pausedSessionInfo!!,
                        onResumeSession = { onNavigateToTimer(pausedSessionInfo!!.session.id) }
                    )
                } else if (lastPuzzle != null) {
                    // Last Puzzle Section
                    LastPuzzleSection(
                        puzzle = lastPuzzle!!,
                        onViewDetails = { onNavigateToPuzzleDetails(lastPuzzle!!.id) }
                    )
                } else {
                    // Empty State
                    EmptyStateSection(onAddPuzzle = onNavigateToNewPuzzle)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            ActionButtonsSection(
                onSearchPuzzle = onNavigateToSearch,
                onStartNewPuzzle = onNavigateToNewPuzzle
            )
        }
    }
}

/**
 * Top app bar for the home screen with centered title
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Puzzle Timer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * Prominent card prompting user to resume a paused puzzle session
 */
@Composable
private fun PausedSessionPrompt(
    pausedSessionInfo: PausedSessionInfo,
    onResumeSession: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Puzzle Image
            AsyncImage(
                model = pausedSessionInfo.puzzle.imageUri,
                contentDescription = "Paused puzzle image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
                    MaterialTheme.colorScheme.surfaceVariant
                ),
                error = androidx.compose.ui.graphics.painter.ColorPainter(
                    MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Prompt Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                Text(
                    text = "Resume Your Puzzle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // Puzzle Name
                Text(
                    text = pausedSessionInfo.puzzle.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // Session Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${pausedSessionInfo.puzzle.pieceCount} pieces",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "Time: ${formatTime(pausedSessionInfo.session.elapsedTimeMillis)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Paused timestamp
                pausedSessionInfo.session.pausedAt?.let { pausedAt ->
                    Text(
                        text = "Paused on ${formatDate(pausedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Resume Button
                Button(
                    onClick = onResumeSession,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Resume Puzzle")
                }
            }
        }
    }
}

/**
 * Section displaying the last completed puzzle
 */
@Composable
private fun LastPuzzleSection(
    puzzle: Puzzle,
    onViewDetails: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Last Puzzle",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Puzzle Image
                AsyncImage(
                    model = puzzle.imageUri,
                    contentDescription = "Puzzle image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    error = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Puzzle Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Completion Date
                    puzzle.lastCompletedDate?.let { date ->
                        Text(
                            text = "Completed on ${formatDate(date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Puzzle Name
                    Text(
                        text = puzzle.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Piece Count and Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${puzzle.pieceCount} pieces",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        puzzle.bestTimeMillis?.let { time ->
                            Text(
                                text = "Final Time: ${formatTime(time)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // View Details Button
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Details")
                    }
                }
            }
        }
    }
}

/**
 * Empty state section shown when no puzzles exist
 */
@Composable
private fun EmptyStateSection(onAddPuzzle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Extension,
            contentDescription = "Puzzle icon",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Ready to start your next jigsaw adventure?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onAddPuzzle,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add a New Puzzle")
        }
    }
}

/**
 * Section with action buttons for searching and starting new puzzles
 */
@Composable
private fun ActionButtonsSection(
    onSearchPuzzle: () -> Unit,
    onStartNewPuzzle: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search for Completed Puzzle Button
        OutlinedButton(
            onClick = onSearchPuzzle,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Search for Completed Puzzle")
        }

        // Start a New Puzzle Button
        OutlinedButton(
            onClick = onStartNewPuzzle,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start a New Puzzle")
        }
    }
}

/**
 * Format a timestamp to a readable date string
 */
@Composable
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Format time in milliseconds to HH:MM:SS format
 */
@Composable
private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = millis / (1000 * 60 * 60)
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}
