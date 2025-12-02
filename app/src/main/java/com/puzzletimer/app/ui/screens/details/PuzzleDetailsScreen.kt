package com.puzzletimer.app.ui.screens.details

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.puzzletimer.app.data.model.Puzzle
import com.puzzletimer.app.data.model.PuzzleSession
import com.puzzletimer.app.ui.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Puzzle Details screen - displays details and history for a puzzle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleDetailsScreen(
    puzzleId: Long,
    puzzleDetailsViewModel: PuzzleDetailsViewModel? = null,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToTimer: (Long) -> Unit
) {
    val actualViewModel = puzzleDetailsViewModel ?: viewModel(
        factory = ViewModelFactory.create(LocalContext.current)
    )
    val puzzle by actualViewModel.puzzle.collectAsState()
    val sessions by actualViewModel.sessions.collectAsState()
    val isLoading by actualViewModel.isLoading.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Edit dialog state
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(puzzleId) {
        actualViewModel.loadPuzzle(puzzleId)
    }

    Scaffold(
        topBar = {
            DetailsTopAppBar(
                onNavigateBack = onNavigateBack,
                onNavigateHome = onNavigateHome,
                onShare = {
                    Toast.makeText(context, "Share feature coming soon!", Toast.LENGTH_SHORT).show()
                }
            )
        },
        floatingActionButton = {
            if (puzzle != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        scope.launch {
                            val sessionId = actualViewModel.startNewSession()
                            sessionId?.let { onNavigateToTimer(it) }
                        }
                    },
                    icon = { Icon(Icons.Default.PlayArrow, "Start") },
                    text = { Text("Do Puzzle Again") }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (isLoading || puzzle == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Header Image
                item {
                    PuzzleHeaderImage(puzzle = puzzle!!)
                }

                // Puzzle Title with Edit Button
                item {
                    PuzzleTitleSection(
                        puzzle = puzzle!!,
                        onEditClick = {
                            showEditDialog = true
                        }
                    )
                }

                // Stats Cards Row
                item {
                    StatsCardsRow(puzzle = puzzle!!)
                }

                // Details Card
                item {
                    DetailsCard(puzzle = puzzle!!)
                }

                // Session History Header
                item {
                    Text(
                        text = "Session History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Session History List
                if (sessions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No sessions yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(sessions) { session ->
                        SessionHistoryItem(
                            session = session,
                            onDelete = { deletedSession ->
                                scope.launch {
                                    // Delete the session
                                    actualViewModel.deleteSession(deletedSession)

                                    // Show snackbar with undo option
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Session deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )

                                    // If user clicks undo, restore the session
                                    if (result == SnackbarResult.ActionPerformed) {
                                        actualViewModel.restoreSession(deletedSession)
                                    }
                                }
                            },
                            onResume = { pausedSession ->
                                // Navigate to timer screen with paused session
                                onNavigateToTimer(pausedSession.id)
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) } // Space for FAB
            }
        }
    }

    // Edit Dialog
    if (showEditDialog && puzzle != null) {
        EditPuzzleDialog(
            puzzle = puzzle!!,
            onDismiss = { showEditDialog = false },
            onSave = { name, pieceCount, imageUri ->
                scope.launch {
                    val result = actualViewModel.updatePuzzleDetails(name, pieceCount, imageUri)
                    if (result.isSuccess) {
                        showEditDialog = false
                        snackbarHostState.showSnackbar(
                            message = "Puzzle updated successfully",
                            duration = SnackbarDuration.Short
                        )
                    } else {
                        val errorMessage = result.exceptionOrNull()?.message ?: "Failed to update puzzle"
                        snackbarHostState.showSnackbar(
                            message = errorMessage,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsTopAppBar(
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onShare: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text("Puzzle Details", fontWeight = FontWeight.Bold) },
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
            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun PuzzleHeaderImage(puzzle: Puzzle) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (puzzle.imageUri != null) {
                AsyncImage(
                    model = puzzle.imageUri,
                    contentDescription = puzzle.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder when no image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Image",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PuzzleTitleSection(
    puzzle: Puzzle,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${puzzle.name}, ${puzzle.pieceCount} pieces",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            // Display brand if available
            puzzle.brand?.let { brand ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = brand,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StatsCardsRow(puzzle: Puzzle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Best Time Card (Yellow/Gold)
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
            ),
            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Best Time",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF996600)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatTime(puzzle.bestTimeMillis),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF996600)
                )
            }
        }

        // Total Completions Card (Gray)
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Completions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = puzzle.totalCompletions.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DetailsCard(puzzle: Puzzle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Average Time
            DetailRow(label = "Average Time", value = formatTime(puzzle.averageTimeMillis))
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Last Completed
            DetailRow(label = "Last Completed", value = formatDate(puzzle.lastCompletedDate))
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // First Completed
            DetailRow(label = "First Completed", value = formatDate(puzzle.firstCompletedDate))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionHistoryItem(
    session: PuzzleSession,
    onDelete: (PuzzleSession) -> Unit,
    onResume: (PuzzleSession) -> Unit
) {
    var isDismissed by remember { mutableStateOf(false) }
    val isPaused = session.pausedAt != null && !session.isCompleted

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd || it == SwipeToDismissBoxValue.EndToStart) {
                isDismissed = true
                onDelete(session)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                SwipeToDismissBoxValue.Settled -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(16.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                    Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (color != Color.Transparent) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isPaused) {
                        Modifier.clickable { onResume(session) }
                    } else {
                        Modifier
                    }
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isPaused) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ),
            border = if (isPaused) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
            } else {
                null
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Date and paused indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatDate(session.startTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPaused) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    // Paused badge
                    if (isPaused) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Paused",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )
                                Text(
                                    text = "Paused",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Right side: Time and resume icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatTime(session.elapsedTimeMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPaused) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    // Resume icon for paused sessions
                    if (isPaused) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long?): String {
    if (millis == null) return "N/A"
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = millis / (1000 * 60 * 60)
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}

private fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return "Never"
    val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Dialog for editing puzzle details (name, piece count, image).
 */
@Composable
private fun EditPuzzleDialog(
    puzzle: Puzzle,
    onDismiss: () -> Unit,
    onSave: (name: String, pieceCount: Int, imageUri: String?) -> Unit
) {
    var editedName by remember { mutableStateOf(puzzle.name) }
    var editedPieceCount by remember { mutableStateOf(puzzle.pieceCount.toString()) }
    var editedImageUri by remember { mutableStateOf<String?>(puzzle.imageUri) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var pieceCountError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistable URI permission
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                editedImageUri = it.toString()
            } catch (e: SecurityException) {
                // If we can't get persistable permission, still use the URI
                // (it might work for this session)
                editedImageUri = it.toString()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Puzzle",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Puzzle Name Field
                OutlinedTextField(
                    value = editedName,
                    onValueChange = {
                        editedName = it
                        nameError = if (it.isBlank()) "Name cannot be empty" else null
                    },
                    label = { Text("Puzzle Name") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Piece Count Field
                OutlinedTextField(
                    value = editedPieceCount,
                    onValueChange = {
                        editedPieceCount = it
                        pieceCountError = when {
                            it.isBlank() -> "Piece count cannot be empty"
                            it.toIntOrNull() == null -> "Must be a valid number"
                            it.toInt() <= 0 -> "Must be greater than 0"
                            else -> null
                        }
                    },
                    label = { Text("Piece Count") },
                    isError = pieceCountError != null,
                    supportingText = pieceCountError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Image Preview and Change Button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Puzzle Image",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Image Preview Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (editedImageUri != null) {
                                AsyncImage(
                                    model = editedImageUri,
                                    contentDescription = "Puzzle Image Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Add Photo",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tap to select image",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Change/Remove Image Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (editedImageUri != null) "Change" else "Add")
                        }

                        if (editedImageUri != null) {
                            OutlinedButton(
                                onClick = { editedImageUri = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate before saving
                    val name = editedName.trim()
                    val pieceCount = editedPieceCount.toIntOrNull()

                    when {
                        name.isBlank() -> nameError = "Name cannot be empty"
                        pieceCount == null -> pieceCountError = "Must be a valid number"
                        pieceCount <= 0 -> pieceCountError = "Must be greater than 0"
                        else -> {
                            onSave(name, pieceCount, editedImageUri)
                        }
                    }
                },
                enabled = nameError == null && pieceCountError == null &&
                        editedName.isNotBlank() && editedPieceCount.toIntOrNull() != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
