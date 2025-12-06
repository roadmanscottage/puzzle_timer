package com.puzzletimer.app.ui.screens.newpuzzle

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.puzzletimer.app.ui.ViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * New Puzzle screen - form to create a new puzzle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPuzzleScreen(
    newPuzzleViewModel: NewPuzzleViewModel? = null,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToPuzzleDetails: (Long) -> Unit,
    onNavigateToSearch: () -> Unit = {}
) {
    val actualViewModel = newPuzzleViewModel ?: viewModel(
        factory = ViewModelFactory.create(LocalContext.current)
    )
    val puzzleName by actualViewModel.puzzleName.collectAsState()
    val brand by actualViewModel.brand.collectAsState()
    val pieceCount by actualViewModel.pieceCount.collectAsState()
    val imageUri by actualViewModel.imageUri.collectAsState()
    val isValid by actualViewModel.isValid.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showImagePickerDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Copy to app storage and update ViewModel
            val savedUri = copyImageToAppStorage(context, it)
            savedUri?.let { actualViewModel.updateImageUri(it.toString()) }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let {
                actualViewModel.updateImageUri(it.toString())
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission granted, launch camera
            val photoFile = createImageFile(context)
            tempCameraUri = FileProvider.getUriForFile(
                context,
                "com.puzzletimer.app.fileprovider",
                photoFile
            )
            cameraLauncher.launch(tempCameraUri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            NewPuzzleTopAppBar(
                onNavigateBack = onNavigateBack,
                onNavigateHome = onNavigateHome
            )
        },
        bottomBar = {
            SavePuzzleButton(
                enabled = isValid,
                onClick = {
                    scope.launch {
                        val puzzleId = actualViewModel.savePuzzle()
                        puzzleId?.let { onNavigateToPuzzleDetails(it) }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Existing Puzzle Prompt Banner
            ExistingPuzzlePrompt(onNavigateToSearch = onNavigateToSearch)

            // Puzzle Name TextField
            OutlinedTextField(
                value = puzzleName,
                onValueChange = { actualViewModel.updatePuzzleName(it) },
                label = { Text("Puzzle Name") },
                placeholder = { Text("e.g., Mountain Scenery") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Brand TextField
            OutlinedTextField(
                value = brand,
                onValueChange = { actualViewModel.updateBrand(it) },
                label = { Text("Brand (Optional)") },
                placeholder = { Text("e.g., Ravensburger") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Number of Pieces TextField
            OutlinedTextField(
                value = pieceCount,
                onValueChange = { actualViewModel.updatePieceCount(it) },
                label = { Text("Number of Pieces") },
                placeholder = { Text("e.g., 1000") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Image Upload Section
            ImageUploadSection(
                imageUri = imageUri,
                onUploadClick = { showImagePickerDialog = true }
            )
        }
    }

    // Image picker dialog
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Choose Image Source") },
            text = { Text("Select an image from your gallery or take a new photo") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImagePickerDialog = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gallery")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImagePickerDialog = false
                        // Check and request camera permission
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                // Permission already granted
                                try {
                                    val photoFile = createImageFile(context)
                                    tempCameraUri = FileProvider.getUriForFile(
                                        context,
                                        "com.puzzletimer.app.fileprovider",
                                        photoFile
                                    )
                                    cameraLauncher.launch(tempCameraUri)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else -> {
                                // Request permission
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Camera")
                }
            }
        )
    }
}

/**
 * Top app bar for New Puzzle screen with back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewPuzzleTopAppBar(
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text("New Puzzle", fontWeight = FontWeight.Bold) },
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
        }
    )
}

/**
 * Bottom button to save the puzzle
 */
@Composable
private fun SavePuzzleButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp)
    ) {
        Text("Save")
    }
}

/**
 * Non-intrusive banner prompting users to search for existing puzzles
 */
@Composable
private fun ExistingPuzzlePrompt(
    onNavigateToSearch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToSearch() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Completed this puzzle before?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Image upload section - shows either upload prompt or selected image
 */
@Composable
private fun ImageUploadSection(
    imageUri: String?,
    onUploadClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (imageUri != null) {
            // Show selected image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = Uri.parse(imageUri),
                    contentDescription = "Selected puzzle image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Change image button
            OutlinedButton(onClick = onUploadClick) {
                Text("Change Image")
            }
        } else {
            // Show upload prompt
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Add Puzzle Image",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Upload a photo of the box",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onUploadClick) {
                        Text("Upload Image")
                    }
                }
            }
        }
    }
}

/**
 * Helper function to create a temporary image file for camera capture
 */
private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("PUZZLE_${timeStamp}_", ".jpg", storageDir)
}

/**
 * Helper function to copy image from gallery to app storage
 */
private fun copyImageToAppStorage(context: Context, sourceUri: Uri): Uri? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PUZZLE_${timeStamp}.jpg"
        val destFile = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Uri.fromFile(destFile)
    } catch (e: Exception) {
        null
    }
}
