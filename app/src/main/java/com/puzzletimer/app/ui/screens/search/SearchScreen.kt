package com.puzzletimer.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.puzzletimer.app.data.model.Puzzle
import com.puzzletimer.app.ui.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sort options for the puzzle list
 */
enum class SortOption {
    NAME,           // Sort alphabetically by name (A-Z)
    DATE_COMPLETED  // Sort by last completed date (most recent first)
}

/**
 * Search screen - search and filter puzzles
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToPuzzleDetails: (Long) -> Unit
) {
    val viewModel: SearchViewModel = viewModel(
        factory = ViewModelFactory.create(LocalContext.current)
    )
    val completedPuzzles by viewModel.completedPuzzles.collectAsState()

    // Load saved sort preference from DataStore
    val savedSortPreference by viewModel.sortPreference.collectAsState()

    // Convert saved string preference to SortOption enum
    val initialSortOption = remember(savedSortPreference) {
        when (savedSortPreference) {
            "DATE_COMPLETED" -> SortOption.DATE_COMPLETED
            else -> SortOption.NAME
        }
    }

    // Sort option state (initialized with saved preference)
    var selectedSortOption by remember(initialSortOption) { mutableStateOf(initialSortOption) }

    // Filter state
    var selectedBrands by remember { mutableStateOf(setOf<String>()) }
    var selectedPieceCounts by remember { mutableStateOf(setOf<Int>()) }

    // Filter visibility state
    var showFilters by remember { mutableStateOf(false) }

    // Calculate active filter count
    val activeFilterCount = selectedBrands.size + selectedPieceCounts.size

    // Snackbar state for undo functionality
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // State for delete confirmation dialog
    var puzzleToDelete by remember { mutableStateOf<Puzzle?>(null) }

    // Apply filtering to completedPuzzles based on selected brands and piece counts
    val filteredPuzzles = remember(completedPuzzles, selectedBrands, selectedPieceCounts) {
        completedPuzzles.filter { puzzle ->
            val brandMatches = selectedBrands.isEmpty() ||
                selectedBrands.contains(puzzle.brand ?: "Unknown")
            val pieceCountMatches = selectedPieceCounts.isEmpty() ||
                selectedPieceCounts.contains(puzzle.pieceCount)
            brandMatches && pieceCountMatches
        }
    }

    // Apply sorting to filteredPuzzles based on selected sort option
    val sortedPuzzles = remember(filteredPuzzles, selectedSortOption) {
        when (selectedSortOption) {
            SortOption.NAME -> filteredPuzzles.sortedBy { it.name.lowercase() }
            SortOption.DATE_COMPLETED -> filteredPuzzles.sortedWith(
                compareByDescending<Puzzle> { it.lastCompletedDate ?: 0L }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Puzzles", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sort options and filter button row
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Sort options
                    SortOptionsRow(
                        selectedOption = selectedSortOption,
                        onOptionSelected = { newOption ->
                            selectedSortOption = newOption
                            // Save the preference to DataStore
                            viewModel.saveSortPreference(newOption.name)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Filter toggle button
                    FilterToggleButton(
                        showFilters = showFilters,
                        activeFilterCount = activeFilterCount,
                        onClick = { showFilters = !showFilters }
                    )
                }

                // Collapsible filter options UI
                AnimatedVisibility(
                    visible = showFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        FilterOptionsSection(
                            completedPuzzles = completedPuzzles,
                            selectedBrands = selectedBrands,
                            onBrandsChanged = { selectedBrands = it },
                            selectedPieceCounts = selectedPieceCounts,
                            onPieceCountsChanged = { selectedPieceCounts = it }
                        )

                        // Clear All Filters button
                        if (activeFilterCount > 0) {
                            Button(
                                onClick = {
                                    selectedBrands = setOf()
                                    selectedPieceCounts = setOf()
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Clear All Filters")
                            }
                        }
                    }
                }
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Content
            if (sortedPuzzles.isEmpty()) {
                // Empty state
                EmptySearchState(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // List of completed puzzles
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = sortedPuzzles,
                        key = { puzzle -> puzzle.id }
                    ) { puzzle ->
                        SwipeToDismissPuzzleCard(
                            puzzle = puzzle,
                            onClick = { onNavigateToPuzzleDetails(puzzle.id) },
                            onDelete = { puzzleToDelete = it }
                        )
                    }
                }
            }
        }

        // Delete confirmation dialog
        puzzleToDelete?.let { puzzle ->
            DeleteConfirmationDialog(
                puzzle = puzzle,
                onConfirm = {
                    val deletedPuzzle = puzzle
                    viewModel.deletePuzzle(deletedPuzzle)
                    puzzleToDelete = null

                    // Show snackbar with undo option
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "\"${deletedPuzzle.name}\" deleted",
                            actionLabel = "Undo",
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            // Restore the deleted puzzle
                            viewModel.restorePuzzle(deletedPuzzle)
                        }
                    }
                },
                onDismiss = { puzzleToDelete = null }
            )
        }
    }
}

/**
 * Row of filter chips for selecting sort option
 */
@Composable
private fun SortOptionsRow(
    selectedOption: SortOption,
    onOptionSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Sort by",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Name (A-Z) filter chip
            FilterChip(
                selected = selectedOption == SortOption.NAME,
                onClick = { onOptionSelected(SortOption.NAME) },
                label = { Text("Name (A-Z)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            // Date Completed filter chip
            FilterChip(
                selected = selectedOption == SortOption.DATE_COMPLETED,
                onClick = { onOptionSelected(SortOption.DATE_COMPLETED) },
                label = { Text("Date Completed") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

/**
 * Filter toggle button with badge showing active filter count
 */
@Composable
private fun FilterToggleButton(
    showFilters: Boolean,
    activeFilterCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = activeFilterCount > 0

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.wrapContentHeight(),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = if (hasActiveFilters) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (hasActiveFilters) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter icon with badge
            if (hasActiveFilters) {
                BadgedBox(
                    badge = {
                        Badge {
                            Text(
                                text = activeFilterCount.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter icon",
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter icon",
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = if (hasActiveFilters) "Filters ($activeFilterCount)" else "Filters",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Filter options section with brand and piece count filters
 */
@Composable
private fun FilterOptionsSection(
    completedPuzzles: List<Puzzle>,
    selectedBrands: Set<String>,
    onBrandsChanged: (Set<String>) -> Unit,
    selectedPieceCounts: Set<Int>,
    onPieceCountsChanged: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Extract unique brands and piece counts from the puzzles
        val uniqueBrands = remember(completedPuzzles) {
            extractUniqueBrands(completedPuzzles)
        }
        val uniquePieceCounts = remember(completedPuzzles) {
            extractUniquePieceCounts(completedPuzzles)
        }

        // Filter by Brand section
        if (uniqueBrands.isNotEmpty()) {
            FilterSection(
                title = "Filter by Brand",
                options = uniqueBrands,
                selectedOptions = selectedBrands,
                onOptionToggled = { brand ->
                    val newSelection = if (selectedBrands.contains(brand)) {
                        selectedBrands - brand
                    } else {
                        selectedBrands + brand
                    }
                    onBrandsChanged(newSelection)
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Filter by Piece Count section
        if (uniquePieceCounts.isNotEmpty()) {
            FilterSection(
                title = "Filter by Piece Count",
                options = uniquePieceCounts.map { it.toString() },
                selectedOptions = selectedPieceCounts.map { it.toString() }.toSet(),
                onOptionToggled = { pieceCountStr ->
                    val pieceCount = pieceCountStr.toInt()
                    val newSelection = if (selectedPieceCounts.contains(pieceCount)) {
                        selectedPieceCounts - pieceCount
                    } else {
                        selectedPieceCounts + pieceCount
                    }
                    onPieceCountsChanged(newSelection)
                }
            )
        }
    }
}

/**
 * Reusable filter section with a title and filter chips
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionToggled: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedOptions.contains(option),
                    onClick = { onOptionToggled(option) },
                    label = { Text(option) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }
}

/**
 * Extract unique brands from puzzles, handling null brands as "Unknown"
 */
private fun extractUniqueBrands(puzzles: List<Puzzle>): List<String> {
    return puzzles
        .map { it.brand ?: "Unknown" }
        .distinct()
        .sorted()
}

/**
 * Extract unique piece counts from puzzles, sorted numerically
 */
private fun extractUniquePieceCounts(puzzles: List<Puzzle>): List<String> {
    return puzzles
        .map { it.pieceCount }
        .distinct()
        .sorted()
        .map { it.toString() }
}

/**
 * Empty state displayed when no completed puzzles exist
 */
@Composable
private fun EmptySearchState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = "No puzzles icon",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Text(
                text = "No Completed Puzzles",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Complete your first puzzle to see it here!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Swipe-to-dismiss wrapper for a puzzle card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissPuzzleCard(
    puzzle: Puzzle,
    onClick: () -> Unit,
    onDelete: (Puzzle) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete(puzzle)
                false // Don't dismiss automatically, wait for dialog confirmation
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Red delete background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        PuzzleCard(
            puzzle = puzzle,
            onClick = onClick
        )
    }
}

/**
 * Delete confirmation dialog
 */
@Composable
private fun DeleteConfirmationDialog(
    puzzle: Puzzle,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(text = "Delete Puzzle?")
        },
        text = {
            Text(text = "Are you sure you want to delete \"${puzzle.name}\"? This will also delete all associated sessions and cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Card displaying a single puzzle in the list
 */
@Composable
private fun PuzzleCard(
    puzzle: Puzzle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

                // Brand and Piece Count
                val brandText = puzzle.brand?.let { "$it • " } ?: ""
                Text(
                    text = "$brandText${puzzle.pieceCount} pieces",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Best Time
                puzzle.bestTimeMillis?.let { time ->
                    Text(
                        text = "Best Time: ${formatTime(time)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/**
 * Format a timestamp to a readable date string
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Format time in milliseconds to HH:MM:SS format
 */
private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = millis / (1000 * 60 * 60)
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}
