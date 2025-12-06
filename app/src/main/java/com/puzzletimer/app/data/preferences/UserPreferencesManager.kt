package com.puzzletimer.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Manager class for handling user preferences using DataStore.
 * Provides methods to save and retrieve user preferences.
 */
class UserPreferencesManager(private val context: Context) {

    companion object {
        // Keys for preferences
        private val SORT_PREFERENCE_KEY = stringPreferencesKey("sort_preference")

        // Default values
        const val DEFAULT_SORT_OPTION = "NAME"
    }

    /**
     * Flow that emits the saved sort preference.
     * Defaults to NAME if no preference is saved.
     */
    val sortPreference: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SORT_PREFERENCE_KEY] ?: DEFAULT_SORT_OPTION
        }

    /**
     * Save the user's sort preference.
     * @param sortOption The sort option to save (e.g., "NAME" or "DATE_COMPLETED")
     */
    suspend fun saveSortPreference(sortOption: String) {
        context.dataStore.edit { preferences ->
            preferences[SORT_PREFERENCE_KEY] = sortOption
        }
    }
}
