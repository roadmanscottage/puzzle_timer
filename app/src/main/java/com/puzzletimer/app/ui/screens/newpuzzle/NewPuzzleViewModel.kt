package com.puzzletimer.app.ui.screens.newpuzzle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzletimer.app.data.model.Puzzle
import com.puzzletimer.app.data.model.PuzzleSession
import com.puzzletimer.app.repository.PuzzleRepository
import com.puzzletimer.app.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for the New Puzzle screen.
 * Manages puzzle creation form state and validation.
 */
class NewPuzzleViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _puzzleName = MutableStateFlow("")
    val puzzleName: StateFlow<String> = _puzzleName.asStateFlow()

    private val _brand = MutableStateFlow("")
    val brand: StateFlow<String> = _brand.asStateFlow()

    private val _pieceCount = MutableStateFlow("")
    val pieceCount: StateFlow<String> = _pieceCount.asStateFlow()

    private val _imageUri = MutableStateFlow<String?>(null)
    val imageUri: StateFlow<String?> = _imageUri.asStateFlow()

    private val _isValid = MutableStateFlow(false)
    val isValid: StateFlow<Boolean> = _isValid.asStateFlow()

    init {
        // Observe form fields and update validation state
        viewModelScope.launch {
            combine(_puzzleName, _pieceCount) { name, count ->
                validateForm(name, count)
            }.collect { valid ->
                _isValid.value = valid
            }
        }
    }

    /**
     * Validate the form fields.
     * Form is valid if name is not empty and piece count is a positive integer.
     */
    private fun validateForm(name: String, count: String): Boolean {
        if (name.isBlank()) return false
        val pieceCountInt = count.toIntOrNull() ?: return false
        return pieceCountInt > 0
    }

    /**
     * Update the puzzle name in the form.
     * @param name The new puzzle name
     */
    fun updatePuzzleName(name: String) {
        _puzzleName.value = name
    }

    /**
     * Update the puzzle brand in the form.
     * @param brand The new puzzle brand
     */
    fun updateBrand(brand: String) {
        _brand.value = brand
    }

    /**
     * Update the piece count in the form.
     * @param count The new piece count as a string
     */
    fun updatePieceCount(count: String) {
        _pieceCount.value = count
    }

    /**
     * Update the image URI in the form.
     * @param uri The new image URI
     */
    fun updateImageUri(uri: String) {
        _imageUri.value = uri
    }

    /**
     * Create a new puzzle and start a timer session.
     * @return The ID of the newly created session, or null if creation failed
     */
    suspend fun startTimer(): Long? {
        if (!_isValid.value) return null

        val pieceCountInt = _pieceCount.value.toIntOrNull() ?: return null

        // Create the puzzle
        val puzzle = Puzzle(
            name = _puzzleName.value.trim(),
            pieceCount = pieceCountInt,
            imageUri = _imageUri.value,
            brand = _brand.value.trim().takeIf { it.isNotEmpty() }
        )

        val puzzleId = puzzleRepository.insertPuzzle(puzzle)

        // Create a new session for this puzzle
        val session = PuzzleSession(
            puzzleId = puzzleId,
            startTime = System.currentTimeMillis()
        )

        val sessionId = sessionRepository.insertSession(session)

        return sessionId
    }
}
