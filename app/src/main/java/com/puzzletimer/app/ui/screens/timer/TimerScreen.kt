package com.puzzletimer.app.ui.screens.timer

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import com.puzzletimer.app.ui.ViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Timer screen - displays the timer for an active session
 */
@Composable
fun TimerScreen(
    sessionId: Long,
    timerViewModel: TimerViewModel? = null,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToPuzzleDetails: (Long) -> Unit
) {
    val actualViewModel = timerViewModel ?: viewModel(
        factory = ViewModelFactory.create(LocalContext.current)
    )
    val puzzleName by actualViewModel.puzzleName.collectAsState()
    val pieceCount by actualViewModel.pieceCount.collectAsState()
    val elapsedTimeMillis by actualViewModel.elapsedTimeMillis.collectAsState()
    val isPaused by actualViewModel.isPaused.collectAsState()

    val scope = rememberCoroutineScope()
    var showFinishDialog by remember { mutableStateOf(false) }
    var showBackDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Handle system back button
    BackHandler {
        showBackDialog = true
    }

    // Load session on first composition
    LaunchedEffect(sessionId) {
        actualViewModel.loadSession(sessionId)
    }

    // Keep screen awake while timer screen is active
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Scaffold(
        topBar = {
            TimerTopAppBar(
                onNavigateBack = { showBackDialog = true },
                onNavigateHome = onNavigateHome
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Puzzle Information
            Text(
                text = puzzleName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$pieceCount pieces",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Timer Display
            TimerDisplay(elapsedTimeMillis)

            Spacer(modifier = Modifier.height(48.dp))

            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showFinishDialog = true },
                    modifier = Modifier.widthIn(min = 120.dp)
                ) {
                    Text("Finish")
                }

                Button(
                    onClick = {
                        scope.launch {
                            if (isPaused) {
                                actualViewModel.resumeTimer()
                            } else {
                                actualViewModel.pauseTimer()
                            }
                        }
                    },
                    modifier = Modifier.widthIn(min = 120.dp)
                ) {
                    Text(if (isPaused) "Resume" else "Pause")
                }
            }
        }
    }

    // Finish Confirmation Dialog
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Finish Puzzle?") },
            text = { Text("Are you sure you want to complete this puzzle session?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val puzzleId = actualViewModel.finishPuzzle()
                            showFinishDialog = false
                            puzzleId?.let { onNavigateToPuzzleDetails(it) }
                        }
                    }
                ) {
                    Text("Finish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Back Navigation Confirmation Dialog
    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            title = { Text("Save Session?") },
            text = { Text("Would you like to save this session to continue later, or abandon it?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            actualViewModel.saveSession()
                            showBackDialog = false
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            actualViewModel.abandonSession()
                            showBackDialog = false
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Abandon")
                }
            }
        )
    }
}

/**
 * Top app bar for the timer screen with centered title
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerTopAppBar(
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text("Puzzle Timer", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onNavigateHome) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            }
        }
    )
}

/**
 * Timer display component with large time and smaller centiseconds
 */
@Composable
private fun TimerDisplay(elapsedTimeMillis: Long) {
    val (mainTime, subTime) = formatTimerDisplay(elapsedTimeMillis)

    Row(
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = mainTime,
            fontSize = 56.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = subTime,
            fontSize = 40.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

/**
 * Format elapsed time in milliseconds to timer display format
 * @param millis Elapsed time in milliseconds
 * @return Pair of main time (HH:MM:SS) and sub time (.CS)
 */
private fun formatTimerDisplay(millis: Long): Pair<String, String> {
    val totalSeconds = millis / 1000
    val centiseconds = (millis % 1000) / 10
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600

    val mainTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    val subTime = String.format(Locale.getDefault(), ".%02d", centiseconds)

    return Pair(mainTime, subTime)
}
