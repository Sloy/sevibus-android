package com.sloy.sevibus.infrastructure.reviews.domain

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.days

/**
 * Shared data source for tracking app start events across different happy moment criteria.
 * Provides centralized app usage tracking to avoid code duplication.
 */
class AppStartTrackingDataSource(
    private val context: Context
) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_start_tracking_prefs")

    companion object {
        private val APP_START_TIMESTAMPS_KEY = stringPreferencesKey("app_start_timestamps")
        const val MIN_APP_OPENS = 3
        val TIME_WINDOW = 30.days
    }

    /**
     * Records the current app start timestamp.
     */
    suspend fun recordAppStart() {
        val now = System.currentTimeMillis()
        context.dataStore.edit { preferences ->
            val existingTimestampsString = preferences[APP_START_TIMESTAMPS_KEY] ?: ""
            val existingTimestamps = parseTimestamps(existingTimestampsString)

            // Add current timestamp and clean up old ones (older than 30 days)
            val thirtyDaysAgo = now - TIME_WINDOW.inWholeMilliseconds
            val updatedTimestamps = (existingTimestamps + now)
                .filter { it >= thirtyDaysAgo }
                .takeLast(10) // Keep at most 10 recent timestamps to avoid unbounded growth

            preferences[APP_START_TIMESTAMPS_KEY] = updatedTimestamps.joinToString(",")
        }
    }

    /**
     * Returns a Flow that emits the current count of app starts within the time window.
     */
    fun observeAppStartCount(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            val timestampsString = preferences[APP_START_TIMESTAMPS_KEY] ?: ""
            val timestamps = parseTimestamps(timestampsString)
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - TIME_WINDOW.inWholeMilliseconds

            // Count app starts within the last 30 days
            timestamps.count { it >= thirtyDaysAgo }
        }
    }

    /**
     * Returns the current count of app starts within the time window (synchronous).
     */
    suspend fun getAppStartCount(): Int {
        return observeAppStartCount().first()
    }

    /**
     * Checks if the app has been started enough times to meet the criteria.
     */
    suspend fun hasEnoughAppStarts(): Boolean {
        return getAppStartCount() >= MIN_APP_OPENS
    }

    private fun parseTimestamps(timestampsString: String): List<Long> {
        return if (timestampsString.isEmpty()) {
            emptyList()
        } else {
            timestampsString.split(",").mapNotNull { it.toLongOrNull() }
        }
    }
}