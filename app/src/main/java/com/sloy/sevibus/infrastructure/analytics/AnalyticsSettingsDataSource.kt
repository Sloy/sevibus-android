package com.sloy.sevibus.infrastructure.analytics

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AnalyticsSettingsDataSource(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "analytics_prefs")
    private val analyticsEnabledKey = booleanPreferencesKey("analytics_enabled")

    fun observeAnalyticsEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[analyticsEnabledKey] ?: true // Default to enabled (opt-out)
        }
    }

    suspend fun isAnalyticsEnabled(): Boolean {
        return observeAnalyticsEnabled().first()
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[analyticsEnabledKey] = enabled
        }
    }
}